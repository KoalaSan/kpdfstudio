package com.buddyapps.pdfstudio.engine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

/**
 * Text extraction and search for PDF files using only the Java/Android framework.
 * It parses PDF objects, inflates FlateDecode streams, reads ToUnicode CMaps and
 * extracts text-showing operators (Tj, TJ, ', ") together with approximate
 * coordinates so matches can be highlighted on the rendered page.
 *
 * Limitations: encrypted documents, CID fonts without a ToUnicode map and PDFs
 * whose text is stored as images cannot be searched. This is reported to the UI
 * as "0 results" rather than silently faked.
 */
public class PdfTextSearchCore {

    public static class SearchResult {
        public final int pageIndex;
        public final String chunkText;
        public final String matched;
        public final int startInChunk;
        public final float x;
        public final float y;
        public final float fontSize;

        public SearchResult(int pageIndex, String chunkText, String matched, int startInChunk,
                            float x, float y, float fontSize) {
            this.pageIndex = pageIndex;
            this.chunkText = chunkText;
            this.matched = matched;
            this.startInChunk = startInChunk;
            this.x = x;
            this.y = y;
            this.fontSize = fontSize;
        }
    }

    private static class Chunk {
        String text;
        float x, y, size;
        Chunk(String text, float x, float y, float size) {
            this.text = text; this.x = x; this.y = y; this.size = size;
        }
    }

    private static class PageData {
        String body;
        String content;
        Map<String, Map<Integer, String>> fonts;
    }

    private static final class PStr { final byte[] bytes; PStr(byte[] b) { bytes = b; } }
    private static final class PName { final String name; PName(String n) { name = n; } }
    private static final class POp { final String op; POp(String o) { op = o; } }

    private static final Pattern OBJ_PATTERN =
            Pattern.compile("(\\d+)\\s+(\\d+)\\s+obj\\b(.*?)\\bendobj", Pattern.DOTALL);
    private static final Pattern REF_PATTERN = Pattern.compile("(\\d+)\\s+\\d+\\s+R");

    public List<SearchResult> search(File file, String query) throws Exception {
        List<SearchResult> results = new ArrayList<>();
        if (file == null || !file.exists() || query == null || query.trim().isEmpty()) return results;
        String needle = query.trim().toLowerCase(Locale.ROOT);

        byte[] bytes = readAll(file);
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);

        Map<Integer, String> objects = new LinkedHashMap<>();
        Matcher om = OBJ_PATTERN.matcher(raw);
        while (om.find()) {
            int num = Integer.parseInt(om.group(1));
            if (!objects.containsKey(num)) objects.put(num, om.group(3));
        }
        if (objects.isEmpty()) return results;

        Map<Integer, String> cmap = new HashMap<>();
        for (String body : objects.values()) {
            String decoded = decodeObjectStream(body);
            if (decoded != null && (decoded.contains("beginbfchar") || decoded.contains("beginbfrange"))) {
                parseCMap(decoded, cmap);
            }
        }

        List<PageData> pages = new ArrayList<>();
        for (Map.Entry<Integer, String> e : objects.entrySet()) {
            String body = e.getValue();
            if (!isPageObject(body)) continue;
            PageData pd = new PageData();
            pd.body = body;
            pd.fonts = fontMaps(body, objects);
            List<Integer> refs = contentRefs(body);
            StringBuilder sb = new StringBuilder();
            for (int num : refs) {
                String cbody = objects.get(num);
                if (cbody == null) continue;
                String decoded = decodeObjectStream(cbody);
                if (decoded != null) sb.append(decoded).append('\n');
            }
            pd.content = sb.toString();
            pages.add(pd);
        }
        if (pages.isEmpty()) {
            // Fallback: treat every decodable stream containing BT as one page.
            for (String body : objects.values()) {
                String decoded = decodeObjectStream(body);
                if (decoded != null && decoded.contains("BT")) {
                    PageData pd = new PageData();
                    pd.body = "";
                    pd.content = decoded;
                    pd.fonts = new HashMap<>();
                    pages.add(pd);
                }
            }
        }

        for (int page = 0; page < pages.size(); page++) {
            PageData pd = pages.get(page);
            List<Chunk> chunks = parseContent(pd.content, pd.fonts, cmap);
            for (Chunk c : chunks) {
                String lower = c.text.toLowerCase(Locale.ROOT);
                int from = 0;
                int idx;
                while ((idx = lower.indexOf(needle, from)) >= 0 && results.size() < 500) {
                    results.add(new SearchResult(page, c.text,
                            c.text.substring(idx, Math.min(c.text.length(), idx + needle.length())),
                            idx, c.x, c.y, c.size));
                    from = idx + needle.length();
                }
            }
        }
        return results;
    }

    /** Returns the number of /Page objects found, or -1 if the structure is unreadable. */
    public int countPages(File file) {
        try {
            byte[] bytes = readAll(file);
            String raw = new String(bytes, StandardCharsets.ISO_8859_1);
            Matcher om = OBJ_PATTERN.matcher(raw);
            int count = 0;
            while (om.find()) if (isPageObject(om.group(3))) count++;
            return count;
        } catch (Exception e) {
            return -1;
        }
    }

    // ------------------------------------------------------------------
    // PDF structure helpers
    // ------------------------------------------------------------------

    private static byte[] readAll(File f) throws IOException {
        FileInputStream in = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[32768];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        return out.toByteArray();
    }

    private static boolean isPageObject(String body) {
        return Pattern.compile("/Type\\s*/Page(?![A-Za-z])").matcher(body).find();
    }

    /** Returns the object numbers referenced by the page's /Contents entry. */
    private static List<Integer> contentRefs(String body) {
        List<Integer> refs = new ArrayList<>();
        String dict = body;
        int streamIdx = body.indexOf("stream");
        if (streamIdx > 0) dict = body.substring(0, streamIdx);
        int i = dict.indexOf("/Contents");
        if (i < 0) return refs;
        int end = dict.length();
        if (dict.charAt(i + 9) == '[') {
            int depth = 0;
            for (int k = i + 9; k < dict.length(); k++) {
                char c = dict.charAt(k);
                if (c == '[') depth++;
                else if (c == ']' && --depth == 0) { end = k + 1; break; }
            }
        }
        String part = dict.substring(i + 9, end);
        Matcher rm = REF_PATTERN.matcher(part);
        while (rm.find()) refs.add(Integer.parseInt(rm.group(1)));
        return refs;
    }

    private static String valueAfter(String body, String key) {
        String dict = body;
        int streamIdx = body.indexOf("stream");
        if (streamIdx > 0) dict = body.substring(0, streamIdx);
        int i = dict.indexOf(key);
        if (i < 0) return null;
        i += key.length();
        while (i < dict.length() && Character.isWhitespace(dict.charAt(i))) i++;
        if (i >= dict.length()) return null;
        int end = i;
        if (dict.charAt(i) == '[') {
            int depth = 0;
            while (end < dict.length()) {
                char c = dict.charAt(end++);
                if (c == '[') depth++;
                else if (c == ']' && --depth == 0) break;
            }
        } else {
            while (end < dict.length() && !Character.isWhitespace(dict.charAt(end)) && dict.charAt(end) != '/') end++;
        }
        return dict.substring(i, end).trim();
    }

    private static String decodeObjectStream(String body) {
        int s = body.indexOf("stream");
        if (s < 0) return null;
        String dict = body.substring(0, s);
        int p = s + 6;
        if (p < body.length() && body.charAt(p) == '\r') p++;
        if (p < body.length() && body.charAt(p) == '\n') p++;
        int e = body.lastIndexOf("endstream");
        if (e < p) e = body.length();
        byte[] data = body.substring(p, e).getBytes(StandardCharsets.ISO_8859_1);
        if (dict.contains("/FlateDecode")) data = inflate(data);
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    private static byte[] inflate(byte[] data) {
        try {
            Inflater inf = new Inflater();
            inf.setInput(data);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            while (!inf.finished()) {
                int n = inf.inflate(buf);
                if (n > 0) out.write(buf, 0, n);
                else if (inf.needsInput() || inf.needsDictionary()) break;
                else break;
            }
            inf.end();
            return out.toByteArray();
        } catch (Exception e) {
            return data;
        }
    }

    // ------------------------------------------------------------------
    // ToUnicode CMap
    // ------------------------------------------------------------------

    private static void parseCMap(String text, Map<Integer, String> map) {
        Matcher bcBlock = Pattern.compile("beginbfchar(.*?)endbfchar", Pattern.DOTALL).matcher(text);
        while (bcBlock.find()) {
            Matcher bc = Pattern.compile("<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>").matcher(bcBlock.group(1));
            while (bc.find()) {
                try {
                    map.put(Integer.parseInt(bc.group(1), 16), hexToString(bc.group(2)));
                } catch (Exception ignored) { }
            }
        }
        Matcher brBlock = Pattern.compile("beginbfrange(.*?)endbfrange", Pattern.DOTALL).matcher(text);
        while (brBlock.find()) {
            Matcher br = Pattern.compile("<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>\\s*<([0-9A-Fa-f]+)>").matcher(brBlock.group(1));
            while (br.find()) {
                try {
                    int start = Integer.parseInt(br.group(1), 16);
                    int end = Integer.parseInt(br.group(2), 16);
                    int base = Integer.parseInt(br.group(3), 16);
                    if (end - start > 4096) end = start + 4096;
                    for (int i = start; i <= end; i++) {
                        map.put(i, hexToString(padHex(Integer.toHexString(base + (i - start)))));
                    }
                } catch (Exception ignored) { }
            }
        }
    }

    /** Maps /F1-style resource names to the ToUnicode CMap of their own font. */
    private static Map<String, Map<Integer, String>> fontMaps(String pageBody, Map<Integer, String> objects) {
        Map<String, Map<Integer, String>> out = new HashMap<>();
        String dict = pageBody;
        int s = dict.indexOf("stream");
        if (s > 0) dict = dict.substring(0, s);

        String fontSource = dict;
        Matcher res = Pattern.compile("/Resources\\s+(\\d+)\\s+\\d+\\s+R").matcher(dict);
        if (res.find()) {
            String ro = objects.get(Integer.parseInt(res.group(1)));
            if (ro != null) fontSource = ro;
        }
        int fi = fontSource.indexOf("/Font");
        if (fi < 0) return out;
        Matcher indirect = Pattern.compile("/Font\\s+(\\d+)\\s+\\d+\\s+R").matcher(fontSource);
        if (indirect.find()) {
            String fo = objects.get(Integer.parseInt(indirect.group(1)));
            if (fo != null) fontSource = fo;
        }
        int begin = fontSource.indexOf("<<", fi);
        if (begin < 0) return out;
        int depth = 0, end = begin;
        for (; end < fontSource.length(); end++) {
            char c = fontSource.charAt(end);
            if (c == '<') depth++;
            else if (c == '>' && --depth == 0) { end++; break; }
        }
        String block = fontSource.substring(begin, Math.min(end, fontSource.length()));
        Matcher fm = Pattern.compile("/([A-Za-z0-9_#-]+)\\s+(\\d+)\\s+\\d+\\s+R").matcher(block);
        while (fm.find()) {
            out.put(fm.group(1), fontCMap(Integer.parseInt(fm.group(2)), objects));
        }
        return out;
    }

    private static Map<Integer, String> fontCMap(int fontObj, Map<Integer, String> objects) {
        Map<Integer, String> map = new HashMap<>();
        String font = objects.get(fontObj);
        if (font == null) return map;
        Matcher tu = Pattern.compile("/ToUnicode\\s+(\\d+)\\s+\\d+\\s+R").matcher(font);
        if (!tu.find()) return map;
        String body = objects.get(Integer.parseInt(tu.group(1)));
        if (body == null) return map;
        String decoded = decodeObjectStream(body);
        if (decoded != null) parseCMap(decoded, map);
        return map;
    }

    private static String padHex(String h) { return (h.length() % 2 == 0) ? h : "0" + h; }

    private static String hexToString(String hex) {
        try {
            String h = padHex(hex);
            byte[] out = new byte[h.length() / 2];
            for (int i = 0; i < out.length; i++)
                out[i] = (byte) Integer.parseInt(h.substring(i * 2, i * 2 + 2), 16);
            return new String(out, StandardCharsets.UTF_16BE);
        } catch (Exception e) {
            return "";
        }
    }

    private static String decodeText(byte[] bytes, Map<Integer, String> map) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < bytes.length) {
            int b1 = bytes[i] & 0xFF;
            int code;
            int adv;
            if (map.containsKey(b1)) { code = b1; adv = 1; }
            else if (i + 1 < bytes.length && map.containsKey((b1 << 8) | (bytes[i + 1] & 0xFF))) {
                code = (b1 << 8) | (bytes[i + 1] & 0xFF); adv = 2;
            } else if (b1 >= 0x80 && i + 1 < bytes.length) {
                code = (b1 << 8) | (bytes[i + 1] & 0xFF); adv = 2;
            } else { code = b1; adv = 1; }

            String mapped = map.get(code);
            if (mapped != null) sb.append(mapped);
            else if (code >= 32 && code < 127) sb.append((char) code);
            else if (code >= 0xA0 && code <= 0xFF) sb.append((char) code);
            i += adv;
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // Content stream tokenizer and text operator interpreter
    // ------------------------------------------------------------------

    private static List<Chunk> parseContent(String content, Map<String, Map<Integer, String>> fonts,
                                            Map<Integer, String> fallbackMap) {
        List<Object> tokens = tokenize(content);
        List<Chunk> chunks = new ArrayList<>();
        List<Object> stack = new ArrayList<>();
        float x = 0, y = 0, size = 12, leading = 0;
        Map<Integer, String> map = fallbackMap;

        for (Object token : tokens) {
            if (!(token instanceof POp)) { stack.add(token); continue; }
            String op = ((POp) token).op;
            int n = stack.size();
            switch (op) {
                case "BT": x = 0; y = 0; break;
                case "Tf":
                    if (n >= 2) {
                        if (stack.get(n - 2) instanceof Double)
                            size = ((Double) stack.get(n - 2)).floatValue();
                        if (stack.get(n - 2) instanceof PName) {
                            Map<Integer, String> fm = fonts.get(((PName) stack.get(n - 2)).name);
                            map = (fm != null) ? fm : fallbackMap;
                        }
                    }
                    break;
                case "TL":
                    if (n >= 1 && stack.get(n - 1) instanceof Double)
                        leading = ((Double) stack.get(n - 1)).floatValue();
                    break;
                case "Tm":
                    if (n >= 6) {
                        Object e = stack.get(n - 2), f = stack.get(n - 1);
                        if (e instanceof Double && f instanceof Double) {
                            x = ((Double) e).floatValue();
                            y = ((Double) f).floatValue();
                        }
                    }
                    break;
                case "Td":
                case "TD":
                    if (n >= 2 && stack.get(n - 2) instanceof Double && stack.get(n - 1) instanceof Double) {
                        x += ((Double) stack.get(n - 2)).floatValue();
                        y += ((Double) stack.get(n - 1)).floatValue();
                        if (op.equals("TD")) leading = -((Double) stack.get(n - 1)).floatValue();
                    }
                    break;
                case "T*": y -= leading; break;
                case "Tj":
                    if (n >= 1 && stack.get(n - 1) instanceof PStr) {
                        String text = decodeText(((PStr) stack.get(n - 1)).bytes, map);
                        if (!text.trim().isEmpty()) chunks.add(new Chunk(text, x, y, size));
                    }
                    break;
                case "TJ":
                    if (n >= 1 && stack.get(n - 1) instanceof List) {
                        List<?> arr = (List<?>) stack.get(n - 1);
                        StringBuilder sb = new StringBuilder();
                        for (Object o : arr) {
                            if (o instanceof PStr) sb.append(decodeText(((PStr) o).bytes, map));
                            else if (o instanceof Double) {
                                float kern = -((Double) o).floatValue() / 1000f * size;
                                x += kern;
                            }
                        }
                        if (!sb.toString().trim().isEmpty()) chunks.add(new Chunk(sb.toString(), x, y, size));
                    }
                    break;
                case "'":
                    y -= leading;
                    if (n >= 1 && stack.get(n - 1) instanceof PStr) {
                        String text = decodeText(((PStr) stack.get(n - 1)).bytes, map);
                        if (!text.trim().isEmpty()) chunks.add(new Chunk(text, x, y, size));
                    }
                    break;
                case "\"":
                    y -= leading;
                    if (n >= 1 && stack.get(n - 1) instanceof PStr) {
                        String text = decodeText(((PStr) stack.get(n - 1)).bytes, map);
                        if (!text.trim().isEmpty()) chunks.add(new Chunk(text, x, y, size));
                    }
                    break;
                default: break;
            }
            stack.clear();
        }
        return chunks;
    }

    private static List<Object> tokenize(String s) {
        int[] pos = new int[]{0};
        return readTokens(s, pos, false);
    }

    private static List<Object> readTokens(String s, int[] pos, boolean stopAtBracket) {
        List<Object> out = new ArrayList<>();
        int i = pos[0];
        while (i < s.length()) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == 0) { i++; continue; }
            if (c == '%') { while (i < s.length() && s.charAt(i) != '\n' && s.charAt(i) != '\r') i++; continue; }
            if (stopAtBracket && c == ']') { pos[0] = i + 1; break; }
            if (c == '(') {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int depth = 1;
                i++;
                while (i < s.length() && depth > 0) {
                    char ch = s.charAt(i++);
                    if (ch == '\\') {
                        if (i >= s.length()) break;
                        char e = s.charAt(i++);
                        switch (e) {
                            case 'n': buf.write('\n'); break;
                            case 'r': buf.write('\r'); break;
                            case 't': buf.write('\t'); break;
                            case 'b': buf.write('\b'); break;
                            case 'f': buf.write('\f'); break;
                            case '(': buf.write('('); break;
                            case ')': buf.write(')'); break;
                            case '\\': buf.write('\\'); break;
                            default:
                                if (e >= '0' && e <= '7') {
                                    int v = e - '0';
                                    for (int k = 0; k < 2 && i < s.length() && s.charAt(i) >= '0' && s.charAt(i) <= '7'; k++)
                                        v = v * 8 + (s.charAt(i++) - '0');
                                    buf.write(v & 0xFF);
                                } else buf.write(e);
                        }
                    } else if (ch == '(') { depth++; buf.write(ch); }
                    else if (ch == ')') { depth--; if (depth > 0) buf.write(ch); }
                    else buf.write(ch & 0xFF);
                }
                out.add(new PStr(buf.toByteArray()));
                continue;
            }
            if (c == '<') {
                if (i + 1 < s.length() && s.charAt(i + 1) == '<') {
                    int end = s.indexOf(">>", i + 2);
                    i = end < 0 ? s.length() : end + 2;
                    continue;
                }
                int end = s.indexOf('>', i + 1);
                if (end < 0) end = s.length();
                String hex = s.substring(i + 1, end).replaceAll("[^0-9A-Fa-f]", "");
                if (hex.length() % 2 == 1) hex += "0";
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                for (int k = 0; k + 1 < hex.length(); k += 2)
                    buf.write(Integer.parseInt(hex.substring(k, k + 2), 16));
                out.add(new PStr(buf.toByteArray()));
                i = end + 1;
                continue;
            }
            if (c == '[') {
                int[] inner = new int[]{i + 1};
                out.add(readTokens(s, inner, true));
                i = inner[0];
                continue;
            }
            if (c == '/') {
                int start = ++i;
                while (i < s.length() && !Character.isWhitespace(s.charAt(i)) && "[]()<>/{}".indexOf(s.charAt(i)) < 0) i++;
                out.add(new PName(s.substring(start, i)));
                continue;
            }
            if (Character.isDigit(c) || c == '.' || c == '-' || c == '+') {
                int start = i;
                while (i < s.length()) {
                    char d = s.charAt(i);
                    if (Character.isDigit(d) || d == '.' || d == '-' || d == '+') i++;
                    else break;
                }
                try { out.add(Double.valueOf(s.substring(start, i))); } catch (Exception ignored) { }
                continue;
            }
            int start = i;
            while (i < s.length()) {
                char d = s.charAt(i);
                if (Character.isWhitespace(d) || "[]()<>/{}".indexOf(d) >= 0) break;
                i++;
            }
            if (i > start) out.add(new POp(s.substring(start, i)));
            else i++;
        }
        pos[0] = i;
        return out;
    }
}
