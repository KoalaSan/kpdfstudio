package com.buddyapps.pdfstudio;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddyapps.pdfstudio.data.DocumentFileStore;
import com.buddyapps.pdfstudio.data.DocumentStorageManager;
import com.buddyapps.pdfstudio.engine.PdfAnnotationCore;
import com.buddyapps.pdfstudio.engine.PdfCreatorCore;
import com.buddyapps.pdfstudio.engine.PdfPageManagerCore;
import com.buddyapps.pdfstudio.engine.PdfRendererCore;
import com.buddyapps.pdfstudio.engine.PdfTextSearchCore;
import com.buddyapps.pdfstudio.model.PdfDocumentItem;
import com.buddyapps.pdfstudio.ui.DesignSystem;
import com.buddyapps.pdfstudio.ui.Icons;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final int REQUEST_PICK_PDF = 101;
    private static final int REQUEST_PICK_IMAGE = 102;
    private static final int REQUEST_CAPTURE_CAMERA = 103;
    private static final int REQUEST_MERGE_IMPORT = 105;
    private static final int REQUEST_CAMERA_PERMISSION = 106;

    private enum Tab { HOME, FILES, TOOLS, SETTINGS }
    private Tab currentTab = Tab.HOME;

    private enum Screen { MAIN, VIEWER, CREATOR, MERGE }
    private Screen currentScreen = Screen.MAIN;

    private ViewGroup rootLayout;
    private LinearLayout mainContentArea;
    private LinearLayout homeListContainer;
    private LinearLayout filesListContainer;
    private DocumentStorageManager storageManager;
    private PdfRendererCore rendererCore;
    private PdfCreatorCore creatorCore;
    private PdfPageManagerCore pageManagerCore;
    private PdfAnnotationCore annotationCore;

    private File activeFile;
    private String activeTitle = "Documento";
    private ImageView viewerImageView;
    private ImageView highlightImageView;
    private Bitmap currentPageBitmap;
    private TextView pageIndicatorView;
    private TextView viewerTitleView;
    private View prevBtn;
    private View nextBtn;
    private IconToggle nightBtn;
    private IconToggle bookmarkBtn;
    private ScrollView viewerVerticalScroll;
    private HorizontalScrollView viewerHorizontalScroll;

    private String searchQuery = "";
    private int selectedCategory = 0; // 0=Todos, 1=Favoritos, 2=Creados

    private List<PdfTextSearchCore.SearchResult> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;

    private List<PdfDocumentItem> mergeDocs = new ArrayList<>();
    private List<String> mergeOrder = new ArrayList<>();
    private LinearLayout mergeListContainer;
    private File cameraOutputFile;
    private boolean darkTheme = true;
    private static final String PREFS = "pdfstudio_prefs";
    private static final String PREF_DARK = "dark_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            startApp();
        } catch (Throwable t) {
            showStartupCrash(t);
        }
    }

    private void startApp() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        darkTheme = prefs.getBoolean(PREF_DARK, true);
        DesignSystem.applyTheme(this, darkTheme);

        storageManager = new DocumentStorageManager(this);
        rendererCore = new PdfRendererCore(this);
        creatorCore = new PdfCreatorCore(this);
        pageManagerCore = new PdfPageManagerCore(this);
        annotationCore = new PdfAnnotationCore(this);

        rootLayout = new LinearLayout(this);
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootLayout.setBackgroundColor(DesignSystem.BG_APP);
        setContentView(rootLayout);
        applyWindowInsets();
        applySystemBars();

        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            handleIncomingPdf(intent.getData());
        } else {
            showMainScreen();
        }
    }

    /** Temporary diagnostics: shows the startup failure on screen instead of closing. */
    private void showStartupCrash(Throwable t) {
        ScrollView scroll = new ScrollView(this);
        TextView text = new TextView(this);
        text.setTextSize(11);
        text.setTextColor(Color.parseColor("#FF6B6B"));
        text.setBackgroundColor(Color.parseColor("#111111"));
        text.setPadding(24, 48, 24, 48);
        text.setText(android.util.Log.getStackTraceString(t));
        scroll.addView(text);
        setContentView(scroll);
    }

    /** Keeps all content below the status bar and above the navigation bar (edge-to-edge on API 36). */
    private void applyWindowInsets() {
        rootLayout.setOnApplyWindowInsetsListener((v, insets) -> {
            int top;
            int bottom;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                top = insets.getInsets(WindowInsets.Type.statusBars()).top;
                bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            rootLayout.setPadding(0, Math.max(0, top), 0, Math.max(0, bottom));
            return insets;
        });
        rootLayout.requestApplyInsets();
    }

    /** Status/navigation bar colours follow the Graphite theme. */
    private void applySystemBars() {
        Window window = getWindow();
        window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(DesignSystem.BG_APP));
        window.setStatusBarColor(DesignSystem.BG_APP);
        window.setNavigationBarColor(DesignSystem.SURFACE_NAV);
        View decor = window.getDecorView();
        int flags = decor.getSystemUiVisibility();
        if (darkTheme) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        }
        decor.setSystemUiVisibility(flags);
    }

    private void setDarkTheme(boolean dark) {
        darkTheme = dark;
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_DARK, dark).apply();
        DesignSystem.applyTheme(this, dark);
        applySystemBars();
        showMainScreen();
    }

    @Override
    public void onBackPressed() {
        if (currentScreen != Screen.MAIN) {
            showMainScreen();
        } else if (currentTab != Tab.HOME) {
            switchTab(Tab.HOME);
        } else {
            super.onBackPressed();
        }
    }

    // ==========================================
    // 1. MAIN APP CONTAINER (WITH BOTTOM NAV)
    // ==========================================
    private void showMainScreen() {
        currentScreen = Screen.MAIN;
        releaseViewerBitmaps();
        rootLayout.removeAllViews();

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        mainContentArea = new LinearLayout(this);
        mainContentArea.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        shell.addView(mainContentArea, contentLp);

        View navBar = buildBottomNavigation();
        shell.addView(navBar);

        rootLayout.addView(shell);
        renderCurrentTabContent();
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        showMainScreen();
    }

    private View buildBottomNavigation() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(DesignSystem.SURFACE_NAV);

        View hairline = new View(this);
        hairline.setBackgroundColor(DesignSystem.BORDER_SUBTLE);
        shell.addView(hairline, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, DesignSystem.dp(this, 0.7f))));

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(DesignSystem.dp(this, 6), DesignSystem.dp(this, 6),
                DesignSystem.dp(this, 6), DesignSystem.dp(this, 6));

        nav.addView(createNavItem("home", "Inicio", currentTab == Tab.HOME, v -> switchTab(Tab.HOME)));
        nav.addView(createNavItem("files", "Biblioteca", currentTab == Tab.FILES, v -> switchTab(Tab.FILES)));
        nav.addView(createNavItem("tools", "Herramientas", currentTab == Tab.TOOLS, v -> switchTab(Tab.TOOLS)));
        nav.addView(createNavItem("settings", "Ajustes", currentTab == Tab.SETTINGS, v -> switchTab(Tab.SETTINGS)));

        shell.addView(nav);
        return shell;
    }

    private View createNavItem(String iconName, String label, boolean selected, View.OnClickListener onClick) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setClickable(true);
        item.setFocusable(true);
        item.setMinimumHeight(DesignSystem.dp(this, 52));
        item.setPadding(0, DesignSystem.dp(this, 4), 0, DesignSystem.dp(this, 2));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(DesignSystem.dp(this, 4), 0, DesignSystem.dp(this, 4), 0);
        item.setLayoutParams(lp);

        int color = selected ? DesignSystem.COLOR_PRIMARY : DesignSystem.TEXT_SECONDARY;

        LinearLayout iconPill = new LinearLayout(this);
        iconPill.setGravity(Gravity.CENTER);
        if (selected) {
            iconPill.setBackground(DesignSystem.createCardDrawable(this,
                    DesignSystem.COLOR_PRIMARY_TINT, 0, 10));
        }
        iconPill.setPadding(DesignSystem.dp(this, 14), DesignSystem.dp(this, 4),
                DesignSystem.dp(this, 14), DesignSystem.dp(this, 4));
        iconPill.addView(Icons.view(this, iconName, 21, color));
        item.addView(iconPill);

        TextView labelView = DesignSystem.text(this, label, 11, color, selected);
        labelView.setPadding(0, DesignSystem.dp(this, 3), 0, 0);
        item.addView(labelView);

        item.setOnClickListener(onClick);
        return item;
    }

    private void renderCurrentTabContent() {
        mainContentArea.removeAllViews();
        switch (currentTab) {
            case HOME:
                mainContentArea.addView(buildHomeTabView());
                break;
            case FILES:
                mainContentArea.addView(buildFilesTabView());
                break;
            case TOOLS:
                mainContentArea.addView(buildToolsTabView());
                break;
            case SETTINGS:
                mainContentArea.addView(buildSettingsTabView());
                break;
        }
    }

    // ==========================================
    // 2. TAB 1: HOME (DASHBOARD)
    // ==========================================
    private View buildHomeTabView() {
        ScrollView scroll = DesignSystem.screenScroll(this);
        LinearLayout content = DesignSystem.screenColumn(this);

        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(this);
        titleCol.setOrientation(LinearLayout.VERTICAL);
        titleCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView appTitle = DesignSystem.text(this, "PDF Studio", DesignSystem.TEXT_DISPLAY,
                DesignSystem.TEXT_PRIMARY, true);
        appTitle.setLetterSpacing(-0.02f);
        titleCol.addView(appTitle);

        TextView appSub = DesignSystem.text(this, "Lectura, edición y herramientas PDF", 13,
                DesignSystem.TEXT_SECONDARY, false);
        appSub.setPadding(0, DesignSystem.dp(this, 2), 0, 0);
        titleCol.addView(appSub);

        headerRow.addView(titleCol);
        headerRow.addView(DesignSystem.pill(this, "100% offline", DesignSystem.COLOR_ACCENT_GREEN));
        content.addView(headerRow);

        EditText searchBar = DesignSystem.createSearchBar(this, "Buscar en la biblioteca");
        searchBar.setText(searchQuery);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                if (homeListContainer != null) populateDocumentList(homeListContainer);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_XL), 0, DesignSystem.dp(this, DesignSystem.SP_L));
        content.addView(searchBar, searchLp);

        LinearLayout actionsRow = new LinearLayout(this);
        actionsRow.setOrientation(LinearLayout.HORIZONTAL);
        actionsRow.setPadding(0, 0, 0, DesignSystem.dp(this, DesignSystem.SP_XL));

        actionsRow.addView(DesignSystem.createQuickActionCard(this, "files", "Abrir",
                DesignSystem.COLOR_PRIMARY, v -> openPdfPicker()));
        actionsRow.addView(DesignSystem.createQuickActionCard(this, "scan", "Escanear",
                DesignSystem.COLOR_ACCENT_GREEN, v -> openCameraCapture()));
        actionsRow.addView(DesignSystem.createQuickActionCard(this, "pen", "Crear",
                DesignSystem.COLOR_ACCENT_AMBER, v -> showCreatorScreen()));
        actionsRow.addView(DesignSystem.createQuickActionCard(this, "merge", "Unir",
                DesignSystem.COLOR_ACCENT_VIOLET, v -> showMergeScreen()));

        content.addView(actionsRow);

        HorizontalScrollView chipScroll = new HorizontalScrollView(this);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setPadding(0, 0, 0, DesignSystem.dp(this, DesignSystem.SP_S));

        LinearLayout chipContainer = new LinearLayout(this);
        chipContainer.setOrientation(LinearLayout.HORIZONTAL);

        chipContainer.addView(DesignSystem.createCategoryChip(this, "Todos", selectedCategory == 0, v -> {
            selectedCategory = 0;
            renderCurrentTabContent();
        }));
        chipContainer.addView(DesignSystem.createCategoryChip(this, "Favoritos", selectedCategory == 1, v -> {
            selectedCategory = 1;
            renderCurrentTabContent();
        }));
        chipContainer.addView(DesignSystem.createCategoryChip(this, "Creados", selectedCategory == 2, v -> {
            selectedCategory = 2;
            renderCurrentTabContent();
        }));

        chipScroll.addView(chipContainer);
        content.addView(chipScroll);

        content.addView(DesignSystem.sectionHeader(this, "Recientes", "Ver todo", v -> switchTab(Tab.FILES)));

        homeListContainer = new LinearLayout(this);
        homeListContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(homeListContainer);
        populateDocumentList(homeListContainer);

        scroll.addView(content);
        return scroll;
    }

    private void populateDocumentList(LinearLayout container) {
        container.removeAllViews();
        List<PdfDocumentItem> recents = storageManager.getRecentDocuments();
        List<PdfDocumentItem> filtered = new ArrayList<>();

        for (PdfDocumentItem item : recents) {
            if (searchQuery.isEmpty() || item.getTitle().toLowerCase().contains(searchQuery)) {
                if (selectedCategory == 1 && !item.isFavorite()) continue;
                if (selectedCategory == 2 && !item.isCreated()) continue;
                filtered.add(item);
            }
        }

        if (filtered.isEmpty()) {
            container.addView(DesignSystem.emptyState(this, "folder",
                    searchQuery.isEmpty() ? "Tu biblioteca está vacía" : "Sin resultados",
                    searchQuery.isEmpty()
                            ? "Abre un PDF, escanea un documento o crea uno desde texto."
                            : "No hay documentos que coincidan con \"" + searchQuery + "\".",
                    searchQuery.isEmpty() ? "Abrir documento" : null,
                    searchQuery.isEmpty() ? v -> openPdfPicker() : null));
        } else {
            for (PdfDocumentItem item : filtered) {
                container.addView(buildModernDocumentCard(item));
            }
        }
    }

    private String pageLabel(int count) {
        return count + (count == 1 ? " página" : " páginas");
    }

    private String docLabel(int count) {
        return count + (count == 1 ? " documento" : " documentos");
    }

    private View buildModernDocumentCard(PdfDocumentItem item) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(DesignSystem.createClickableCardDrawable(this,
                DesignSystem.SURFACE_CARD, DesignSystem.SURFACE_CARD_PRESSED, DesignSystem.RADIUS_LG));
        card.setPadding(DesignSystem.dp(this, DesignSystem.SP_M), DesignSystem.dp(this, DesignSystem.SP_M),
                DesignSystem.dp(this, DesignSystem.SP_S), DesignSystem.dp(this, DesignSystem.SP_M));
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, DesignSystem.dp(this, DesignSystem.SP_M));
        card.setLayoutParams(lp);

        View badge = DesignSystem.iconBadge(this, "doc", DesignSystem.COLOR_ACCENT_ROSE, 44, 22);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                DesignSystem.dp(this, 44), DesignSystem.dp(this, 44));
        badgeLp.setMargins(0, 0, DesignSystem.dp(this, DesignSystem.SP_M), 0);
        card.addView(badge, badgeLp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView docName = DesignSystem.text(this, item.getTitle(), 15,
                DesignSystem.TEXT_PRIMARY, true);
        docName.setSingleLine(true);
        docName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textCol.addView(docName);

        TextView meta = DesignSystem.text(this,
                pageLabel(item.getPageCount()) + "  ·  " + Math.max(1, item.getSizeBytes() / 1024) + " KB",
                DesignSystem.TEXT_CAPTION, DesignSystem.TEXT_SECONDARY, false);
        meta.setPadding(0, DesignSystem.dp(this, 3), 0, 0);
        textCol.addView(meta);

        card.addView(textCol);

        View favBtn = DesignSystem.iconButton(this, item.isFavorite() ? "starfill" : "star",
                item.isFavorite() ? DesignSystem.COLOR_ACCENT_AMBER : DesignSystem.TEXT_TERTIARY, v -> {
                    storageManager.toggleFavorite(item.getId(), item.isFavorite());
                    if (homeListContainer != null) populateDocumentList(homeListContainer);
                    if (filesListContainer != null) populateDocumentList(filesListContainer);
                });
        card.addView(favBtn);

        card.setOnClickListener(v -> {
            File f = new File(item.getPath());
            if (f.exists()) {
                openPdfFile(f, item.getTitle());
            } else {
                Toast.makeText(this, "El archivo original ya no está en el almacenamiento", Toast.LENGTH_SHORT).show();
            }
        });

        return card;
    }

    private View buildEmptyStateView(String title, String desc) {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);
        empty.setBackground(DesignSystem.createCardDrawable(this, DesignSystem.SURFACE_CARD, DesignSystem.BORDER_SUBTLE, 14));
        empty.setPadding(DesignSystem.dp(this, 24), DesignSystem.dp(this, 36), DesignSystem.dp(this, 24), DesignSystem.dp(this, 36));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, DesignSystem.dp(this, 8), 0, DesignSystem.dp(this, 16));
        empty.setLayoutParams(lp);

        TextView icon = new TextView(this);
        icon.setText("📑");
        icon.setTextSize(40);
        icon.setGravity(Gravity.CENTER);
        empty.addView(icon);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextColor(DesignSystem.TEXT_PRIMARY);
        t.setTextSize(16);
        t.getPaint().setFakeBoldText(true);
        t.setPadding(0, DesignSystem.dp(this, 12), 0, DesignSystem.dp(this, 6));
        empty.addView(t);

        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextColor(DesignSystem.TEXT_SECONDARY);
        d.setTextSize(13);
        d.setGravity(Gravity.CENTER);
        empty.addView(d);

        Button openAction = DesignSystem.createPrimaryButton(this, "Abrir Documento", v -> openPdfPicker());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, DesignSystem.dp(this, 18), 0, 0);
        empty.addView(openAction, btnLp);

        return empty;
    }

    // ==========================================
    // 3. TAB 2: FILES EXPLORER
    // ==========================================
    private View buildFilesTabView() {
        ScrollView scroll = DesignSystem.screenScroll(this);
        LinearLayout content = DesignSystem.screenColumn(this);

        content.addView(DesignSystem.screenHeader(this, "Biblioteca",
                "Todos los PDF guardados en este dispositivo"));

        List<PdfDocumentItem> docs = storageManager.getRecentDocuments();
        long totalBytes = 0;
        for (PdfDocumentItem it : docs) totalBytes += it.getSizeBytes();

        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_L), 0, 0);
        statsRow.addView(DesignSystem.pill(this, docLabel(docs.size()), DesignSystem.COLOR_PRIMARY));
        LinearLayout.LayoutParams gap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        gap.setMargins(DesignSystem.dp(this, DesignSystem.SP_S), 0, 0, 0);
        statsRow.addView(DesignSystem.pill(this, Math.max(1, totalBytes / 1024) + " KB usados",
                DesignSystem.TEXT_SECONDARY), gap);
        content.addView(statsRow);

        EditText searchBar = DesignSystem.createSearchBar(this, "Buscar por nombre");
        searchBar.setText(searchQuery);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim().toLowerCase();
                if (filesListContainer != null) populateDocumentList(filesListContainer);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        searchLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_L), 0, DesignSystem.dp(this, DesignSystem.SP_M));
        content.addView(searchBar, searchLp);

        HorizontalScrollView chipScroll = new HorizontalScrollView(this);
        chipScroll.setHorizontalScrollBarEnabled(false);
        chipScroll.setPadding(0, 0, 0, DesignSystem.dp(this, DesignSystem.SP_S));

        LinearLayout chipContainer = new LinearLayout(this);
        chipContainer.setOrientation(LinearLayout.HORIZONTAL);
        chipContainer.addView(DesignSystem.createCategoryChip(this, "Todos", selectedCategory == 0, v -> {
            selectedCategory = 0;
            renderCurrentTabContent();
        }));
        chipContainer.addView(DesignSystem.createCategoryChip(this, "Favoritos", selectedCategory == 1, v -> {
            selectedCategory = 1;
            renderCurrentTabContent();
        }));
        chipContainer.addView(DesignSystem.createCategoryChip(this, "Creados", selectedCategory == 2, v -> {
            selectedCategory = 2;
            renderCurrentTabContent();
        }));
        chipScroll.addView(chipContainer);
        content.addView(chipScroll);

        content.addView(DesignSystem.sectionHeader(this, "Documentos", null, null));

        filesListContainer = new LinearLayout(this);
        filesListContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(filesListContainer);
        populateDocumentList(filesListContainer);

        Button importBtn = DesignSystem.createPrimaryButton(this, "Importar PDF", v -> openPdfPicker());
        LinearLayout.LayoutParams importLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        importLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_S), 0, 0);
        content.addView(importBtn, importLp);

        scroll.addView(content);
        return scroll;
    }

    // ==========================================
    // 4. TAB 3: TOOLS (PDF TOOLKIT FULLY FUNCTIONAL)
    // ==========================================
    private View buildToolsTabView() {
        ScrollView scroll = DesignSystem.screenScroll(this);
        LinearLayout content = DesignSystem.screenColumn(this);

        content.addView(DesignSystem.screenHeader(this, "Herramientas",
                "Todo se procesa en el dispositivo, sin subir archivos"));

        TextView organize = DesignSystem.sectionTitle(this, "Organizar");
        organize.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_XL), 0, DesignSystem.dp(this, DesignSystem.SP_M));
        content.addView(organize);

        content.addView(DesignSystem.createToolRow(this, "merge", "Unir PDFs",
                "Combina varios documentos y elige el orden de las páginas.",
                DesignSystem.COLOR_ACCENT_BLUE, v -> showMergeScreen()));

        content.addView(DesignSystem.createToolRow(this, "scissors", "Dividir o extraer",
                "Aísla un rango de páginas en un PDF nuevo.",
                DesignSystem.COLOR_ACCENT_VIOLET, v -> {
                    if (activeFile != null) {
                        showViewerScreen(activeTitle);
                        showExtractRangeDialog();
                    } else {
                        loadDemoDocument();
                        showExtractRangeDialog();
                    }
                }));

        content.addView(DesignSystem.createToolRow(this, "compress", "Comprimir PDF",
                "Reduce el peso optimizando las imágenes internas.",
                DesignSystem.COLOR_ACCENT_ROSE, v -> {
                    if (activeFile != null) {
                        compressActiveDocument();
                    } else {
                        loadDemoDocument();
                        compressActiveDocument();
                    }
                }));

        TextView create = DesignSystem.sectionTitle(this, "Crear");
        create.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, DesignSystem.dp(this, DesignSystem.SP_M));
        content.addView(create);

        content.addView(DesignSystem.createToolRow(this, "scan", "Escanear con cámara",
                "Fotografía un documento y conviértelo en PDF A4.",
                DesignSystem.COLOR_ACCENT_GREEN, v -> openCameraCapture()));

        content.addView(DesignSystem.createToolRow(this, "image", "Fotos a PDF",
                "Convierte imágenes de la galería en un documento.",
                DesignSystem.COLOR_ACCENT_BLUE, v -> openImagePicker()));

        content.addView(DesignSystem.createToolRow(this, "pen", "Creador de texto",
                "Redacta y exporta un PDF con maquetación automática.",
                DesignSystem.COLOR_ACCENT_AMBER, v -> showCreatorScreen()));

        TextView test = DesignSystem.sectionTitle(this, "Pruebas");
        test.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, DesignSystem.dp(this, DesignSystem.SP_M));
        content.addView(test);

        content.addView(DesignSystem.createToolRow(this, "book", "Documento de prueba",
                "Genera un PDF de ejemplo para probar el visor y la búsqueda.",
                DesignSystem.COLOR_ACCENT_VIOLET, v -> loadDemoDocument()));

        scroll.addView(content);
        return scroll;
    }

    // ==========================================
    // 5. TAB 4: SETTINGS
    // ==========================================
    private View buildSettingsTabView() {
        ScrollView scroll = DesignSystem.screenScroll(this);
        LinearLayout content = DesignSystem.screenColumn(this);

        content.addView(DesignSystem.screenHeader(this, "Ajustes",
                "Apariencia, lectura y almacenamiento"));

        // ---- Appearance ----
        content.addView(DesignSystem.sectionHeader(this, "Apariencia", null, null));

        LinearLayout themeRow = buildSettingRow("palette", "Tema",
                "Graphite oscuro o claro en toda la app", null);

        LinearLayout segmented = new LinearLayout(this);
        segmented.setOrientation(LinearLayout.HORIZONTAL);
        View darkChip = DesignSystem.createCategoryChip(this, "Oscuro", darkTheme, v -> {
            if (!darkTheme) setDarkTheme(true);
        });
        View lightChip = DesignSystem.createCategoryChip(this, "Claro", !darkTheme, v -> {
            if (darkTheme) setDarkTheme(false);
        });
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        chipLp.setMargins(0, 0, DesignSystem.dp(this, 6), 0);
        segmented.addView(darkChip, chipLp);
        segmented.addView(lightChip);
        themeRow.addView(segmented);
        content.addView(themeRow);

        Button nightToggle = DesignSystem.createSecondaryButton(this,
                rendererCore.isNightMode() ? "Activado" : "Desactivado", v -> {
                    rendererCore.setNightMode(!rendererCore.isNightMode());
                    renderCurrentTabContent();
                    Toast.makeText(this, "Modo lectura: " + (rendererCore.isNightMode() ? "Activado" : "Desactivado"),
                            Toast.LENGTH_SHORT).show();
                });
        content.addView(buildSettingRow("moon", "Modo lectura nocturna",
                "Invierte los colores de la página en el visor", nightToggle));

        // ---- Storage ----
        content.addView(DesignSystem.sectionHeader(this, "Almacenamiento", null, null));

        List<PdfDocumentItem> docs = storageManager.getRecentDocuments();
        long totalBytes = 0;
        for (PdfDocumentItem it : docs) totalBytes += it.getSizeBytes();

        TextView storageValue = DesignSystem.text(this,
                docLabel(docs.size()) + " · " + Math.max(1, totalBytes / 1024) + " KB",
                DesignSystem.TEXT_CAPTION, DesignSystem.TEXT_SECONDARY, false);
        content.addView(buildSettingRow("storage", "Biblioteca local",
                "Los archivos viven solo en este dispositivo", storageValue));

        Button openLibrary = DesignSystem.createPrimaryButton(this, "Abrir biblioteca",
                v -> switchTab(Tab.FILES));
        LinearLayout.LayoutParams libLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        libLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_S), 0, 0);
        content.addView(openLibrary, libLp);

        // ---- About ----
        content.addView(DesignSystem.sectionHeader(this, "Acerca de", null, null));

        LinearLayout about = new LinearLayout(this);
        about.setOrientation(LinearLayout.VERTICAL);
        about.setBackground(DesignSystem.createCardDrawable(this,
                DesignSystem.SURFACE_CARD, DesignSystem.BORDER_SUBTLE, DesignSystem.RADIUS_LG));
        about.setPadding(DesignSystem.dp(this, DesignSystem.SP_L), DesignSystem.dp(this, DesignSystem.SP_L),
                DesignSystem.dp(this, DesignSystem.SP_L), DesignSystem.dp(this, DesignSystem.SP_L));

        LinearLayout aboutHeader = new LinearLayout(this);
        aboutHeader.setOrientation(LinearLayout.HORIZONTAL);
        aboutHeader.setGravity(Gravity.CENTER_VERTICAL);
        aboutHeader.addView(DesignSystem.iconBadge(this, "doc", DesignSystem.COLOR_PRIMARY, 40, 20),
                new LinearLayout.LayoutParams(DesignSystem.dp(this, 40), DesignSystem.dp(this, 40)));

        LinearLayout aboutTitles = new LinearLayout(this);
        aboutTitles.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams aboutLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        aboutLp.setMargins(DesignSystem.dp(this, DesignSystem.SP_M), 0, 0, 0);
        aboutTitles.setLayoutParams(aboutLp);
        aboutTitles.addView(DesignSystem.text(this, "PDF Studio", DesignSystem.TEXT_SUBTITLE,
                DesignSystem.TEXT_PRIMARY, true));
        aboutTitles.addView(DesignSystem.text(this, "v3.4 · 100% offline", DesignSystem.TEXT_CAPTION,
                DesignSystem.TEXT_SECONDARY, false));
        aboutHeader.addView(aboutTitles);
        about.addView(aboutHeader);

        TextView aboutBody = DesignSystem.bodyText(this,
                "Lector, editor y organizador de PDF sin telemetría ni servidores remotos. "
                        + "Renderizado con PdfRenderer, anotaciones vectoriales y manipulación de páginas.");
        aboutBody.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, 0);
        about.addView(aboutBody);

        content.addView(about);

        scroll.addView(content);
        return scroll;
    }

    /** Row with icon, title/description and an optional trailing view. */
    private LinearLayout buildSettingRow(String iconName, String title, String desc, View trailing) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(DesignSystem.createCardDrawable(this,
                DesignSystem.SURFACE_CARD, DesignSystem.BORDER_SUBTLE, DesignSystem.RADIUS_LG));
        row.setPadding(DesignSystem.dp(this, DesignSystem.SP_L), DesignSystem.dp(this, DesignSystem.SP_M),
                DesignSystem.dp(this, DesignSystem.SP_L), DesignSystem.dp(this, DesignSystem.SP_M));
        row.setMinimumHeight(DesignSystem.dp(this, 64));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, DesignSystem.dp(this, DesignSystem.SP_S));
        row.setLayoutParams(lp);

        row.addView(DesignSystem.iconBadge(this, iconName, DesignSystem.COLOR_PRIMARY, 36, 18),
                new LinearLayout.LayoutParams(DesignSystem.dp(this, 36), DesignSystem.dp(this, 36)));

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.setMargins(DesignSystem.dp(this, DesignSystem.SP_M), 0, DesignSystem.dp(this, DesignSystem.SP_S), 0);
        textCol.setLayoutParams(textLp);
        textCol.addView(DesignSystem.text(this, title, 15, DesignSystem.TEXT_PRIMARY, true));
        textCol.addView(DesignSystem.text(this, desc, DesignSystem.TEXT_CAPTION,
                DesignSystem.TEXT_SECONDARY, false));
        row.addView(textCol);

        if (trailing != null) row.addView(trailing);
        return row;
    }

    // ==========================================
    // 6. IMMERSIVE VIEWER SCREEN (WITH TOOLS DOCK)
    // ==========================================
    private void showViewerScreen(String title) {
        currentScreen = Screen.VIEWER;
        activeTitle = title;
        releaseViewerBitmaps();
        rootLayout.removeAllViews();
        searchResults = new ArrayList<>();
        currentSearchIndex = -1;
        currentPageBitmap = null;

        LinearLayout viewer = new LinearLayout(this);
        viewer.setOrientation(LinearLayout.VERTICAL);
        viewer.setBackgroundColor(DesignSystem.BG_APP);
        viewer.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(DesignSystem.SURFACE_HEADER);
        header.setPadding(DesignSystem.dp(this, 6), DesignSystem.dp(this, 6),
                DesignSystem.dp(this, 6), DesignSystem.dp(this, 6));

        header.addView(DesignSystem.iconButton(this, "back", DesignSystem.TEXT_PRIMARY,
                v -> showMainScreen()));

        viewerTitleView = DesignSystem.text(this, title, 16, DesignSystem.TEXT_PRIMARY, true);
        viewerTitleView.setSingleLine(true);
        viewerTitleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        viewerTitleView.setPadding(DesignSystem.dp(this, 8), 0, DesignSystem.dp(this, 8), 0);
        header.addView(viewerTitleView, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        bookmarkBtn = new IconToggle(isCurrentPageBookmarked() ? "starfill" : "star",
                isCurrentPageBookmarked() ? DesignSystem.COLOR_ACCENT_AMBER : DesignSystem.TEXT_SECONDARY,
                v -> toggleBookmarkCurrentPage());
        header.addView(bookmarkBtn);

        nightBtn = new IconToggle(rendererCore.isNightMode() ? "sun" : "moon",
                rendererCore.isNightMode() ? DesignSystem.COLOR_ACCENT_AMBER : DesignSystem.TEXT_SECONDARY,
                v -> toggleNightMode());
        header.addView(nightBtn);

        header.addView(DesignSystem.iconButton(this, "search", DesignSystem.TEXT_SECONDARY,
                v -> showTextSearchDialog()));
        header.addView(DesignSystem.iconButton(this, "hash", DesignSystem.TEXT_SECONDARY,
                v -> showPageJumpDialog()));
        header.addView(DesignSystem.iconButton(this, "share", DesignSystem.TEXT_SECONDARY,
                v -> shareCurrentDocument()));

        viewer.addView(header, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View headerLine = new View(this);
        headerLine.setBackgroundColor(DesignSystem.BORDER_SUBTLE);
        viewer.addView(headerLine, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, DesignSystem.dp(this, 0.7f))));

        // Viewport with a transparent overlay used to highlight search matches.
        viewerVerticalScroll = new ScrollView(this);
        viewerVerticalScroll.setFillViewport(true);
        viewerVerticalScroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);

        viewerHorizontalScroll = new HorizontalScrollView(this);
        viewerHorizontalScroll.setFillViewport(true);
        viewerHorizontalScroll.setHorizontalScrollBarEnabled(false);

        FrameLayout imageFrame = new FrameLayout(this);
        viewerImageView = new ImageView(this);
        viewerImageView.setAdjustViewBounds(true);
        imageFrame.addView(viewerImageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        highlightImageView = new ImageView(this);
        imageFrame.addView(highlightImageView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        LinearLayout imageContainer = new LinearLayout(this);
        imageContainer.setOrientation(LinearLayout.VERTICAL);
        imageContainer.setGravity(Gravity.CENTER);
        imageContainer.setPadding(DesignSystem.dp(this, DesignSystem.SP_M), DesignSystem.dp(this, DesignSystem.SP_M),
                DesignSystem.dp(this, DesignSystem.SP_M), DesignSystem.dp(this, DesignSystem.SP_M));
        imageContainer.addView(imageFrame);

        viewerHorizontalScroll.addView(imageContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewerVerticalScroll.addView(viewerHorizontalScroll, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        viewer.addView(viewerVerticalScroll, scrollLp);

        // Bottom dock: navigation + zoom, then document tools.
        LinearLayout dock = new LinearLayout(this);
        dock.setOrientation(LinearLayout.VERTICAL);
        dock.setBackgroundColor(DesignSystem.SURFACE_NAV);
        dock.setPadding(DesignSystem.dp(this, 6), DesignSystem.dp(this, 4),
                DesignSystem.dp(this, 6), DesignSystem.dp(this, 6));

        View dockLine = new View(this);
        dockLine.setBackgroundColor(DesignSystem.BORDER_SUBTLE);
        dock.addView(dockLine, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, DesignSystem.dp(this, 0.7f))));

        LinearLayout navRow = buildDockRow();
        prevBtn = dockAction("back", "Anterior", v -> {
            if (rendererCore.getActivePageIndex() > 0) renderCurrentPage(rendererCore.getActivePageIndex() - 1);
        });
        addDockView(navRow, prevBtn, 1f);

        pageIndicatorView = DesignSystem.text(this, "Pág - / -", 13, DesignSystem.COLOR_PRIMARY, true);
        pageIndicatorView.setGravity(Gravity.CENTER);
        pageIndicatorView.setBackground(DesignSystem.createCardDrawable(this,
                DesignSystem.COLOR_PRIMARY_TINT, 0, DesignSystem.RADIUS_SM));
        pageIndicatorView.setPadding(DesignSystem.dp(this, DesignSystem.SP_S), DesignSystem.dp(this, 6),
                DesignSystem.dp(this, DesignSystem.SP_S), DesignSystem.dp(this, 6));
        addDockView(navRow, pageIndicatorView, 1.6f);

        nextBtn = dockAction("chevron", "Siguiente", v -> {
            if (rendererCore.getActivePageIndex() < rendererCore.getPageCount() - 1)
                renderCurrentPage(rendererCore.getActivePageIndex() + 1);
        });
        addDockView(navRow, nextBtn, 1f);
        addDockView(navRow, dockAction("zoomout", "Zoom -", v -> {
            rendererCore.setZoom(rendererCore.getZoom() - 0.25f);
            renderCurrentPage(rendererCore.getActivePageIndex());
        }), 1f);
        addDockView(navRow, dockAction("zoomin", "Zoom +", v -> {
            rendererCore.setZoom(rendererCore.getZoom() + 0.25f);
            renderCurrentPage(rendererCore.getActivePageIndex());
        }), 1f);
        dock.addView(wrapDockRow(navRow));

        LinearLayout toolsRow = buildDockRow();
        addDockView(toolsRow, dockAction("rotate", "Girar", v -> {
            rendererCore.rotateClockwise();
            renderCurrentPage(rendererCore.getActivePageIndex());
            Toast.makeText(this, "Rotación: " + rendererCore.getRotationDegrees() + "°", Toast.LENGTH_SHORT).show();
        }), 1f);
        addDockView(toolsRow, dockAction("pen", "Anotar", v -> openAnnotationModal()), 1f);
        addDockView(toolsRow, dockAction("scissors", "Extraer", v -> showExtractRangeDialog()), 1f);
        addDockView(toolsRow, dockAction("trash", "Borrar", v -> confirmDeleteCurrentPage()), 1f);
        addDockView(toolsRow, dockAction("search", "Buscar", v -> showTextSearchDialog()), 1f);
        dock.addView(wrapDockRow(toolsRow));

        viewer.addView(dock);
        rootLayout.addView(viewer);
        renderCurrentPage(rendererCore.getActivePageIndex());
    }

    /** Icon + micro label used in the viewer dock. */
    private View dockAction(String icon, String label, View.OnClickListener onClick) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setClickable(true);
        item.setFocusable(true);
        item.setPadding(DesignSystem.dp(this, 2), DesignSystem.dp(this, 6),
                DesignSystem.dp(this, 2), DesignSystem.dp(this, 4));
        item.setMinimumWidth(DesignSystem.dp(this, DesignSystem.TOUCH_MIN));
        item.setMinimumHeight(DesignSystem.dp(this, 48));

        item.addView(Icons.view(this, icon, 20, DesignSystem.TEXT_SECONDARY));
        TextView l = DesignSystem.text(this, label, 9, DesignSystem.TEXT_TERTIARY, false);
        l.setSingleLine(true);
        l.setPadding(0, DesignSystem.dp(this, 3), 0, 0);
        item.addView(l);

        item.setOnClickListener(onClick);
        return item;
    }

    /** Small icon button whose glyph and tint can change at runtime. */
    private class IconToggle extends LinearLayout {
        private String icon;
        private int tint;

        IconToggle(String icon, int tint, View.OnClickListener onClick) {
            super(MainActivity.this);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER);
            setMinimumWidth(DesignSystem.dp(MainActivity.this, DesignSystem.TOUCH_MIN));
            setMinimumHeight(DesignSystem.dp(MainActivity.this, DesignSystem.TOUCH_MIN));
            setClickable(true);
            setFocusable(true);
            this.icon = icon;
            this.tint = tint;
            addView(Icons.view(MainActivity.this, icon, 22, tint));
            setOnClickListener(onClick);
        }

        void setIcon(String newIcon, int newTint) {
            this.icon = newIcon;
            this.tint = newTint;
            removeAllViews();
            addView(Icons.view(MainActivity.this, icon, 22, tint));
        }
    }

    private LinearLayout buildDockRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        return row;
    }

    private HorizontalScrollView wrapDockRow(LinearLayout row) {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setFillViewport(true);
        scroll.addView(row, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private void addDockView(LinearLayout row, View view, float weight) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
        lp.setMargins(DesignSystem.dp(this, 2), 0, DesignSystem.dp(this, 2), 0);
        view.setMinimumWidth(DesignSystem.dp(this, 44));
        view.setMinimumHeight(DesignSystem.dp(this, 38));
        row.addView(view, lp);
    }

    private void renderCurrentPage(int index) {
        Bitmap previousBitmap = currentPageBitmap;
        Bitmap bitmap = rendererCore.renderPage(index, viewerImageView);
        if (bitmap != null) {
            currentPageBitmap = bitmap;
            pageIndicatorView.setText("Pág " + (index + 1) + " / " + rendererCore.getPageCount());
            prevBtn.setEnabled(index > 0);
            prevBtn.setAlpha(index > 0 ? 1f : 0.35f);
            nextBtn.setEnabled(index < rendererCore.getPageCount() - 1);
            nextBtn.setAlpha(index < rendererCore.getPageCount() - 1 ? 1f : 0.35f);
            if (bookmarkBtn != null) {
                boolean marked = isCurrentPageBookmarked();
                bookmarkBtn.setIcon(marked ? "starfill" : "star",
                        marked ? DesignSystem.COLOR_ACCENT_AMBER : DesignSystem.TEXT_SECONDARY);
            }
            viewerVerticalScroll.scrollTo(0, 0);
            viewerHorizontalScroll.scrollTo(0, 0);
            drawHighlightOverlay(index);
            recyclePageBitmap(previousBitmap, bitmap);
        } else {
            // OOM path: rendererCore already reset zoom to 1.0, retry once.
            if (rendererCore.getZoom() > 1.0f) {
                rendererCore.setZoom(1.0f);
                renderCurrentPage(index);
            } else {
                Toast.makeText(this, "No se pudo renderizar la página", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Frees the previous page bitmap only after the new one is on screen. */
    private void recyclePageBitmap(Bitmap oldBitmap, Bitmap newBitmap) {
        if (oldBitmap != null && oldBitmap != newBitmap && !oldBitmap.isRecycled()) {
            oldBitmap.recycle();
        }
    }

    /** Called when leaving the viewer: the ImageViews are removed, so their bitmaps can go too. */
    private void releaseViewerBitmaps() {
        if (currentPageBitmap != null && !currentPageBitmap.isRecycled()) currentPageBitmap.recycle();
        currentPageBitmap = null;
        if (highlightImageView != null) setHighlightBitmap(null);
        highlightImageView = null;
        viewerImageView = null;
    }

    /** Replaces the highlight overlay and releases the bitmap it previously held. */
    private void setHighlightBitmap(Bitmap overlay) {
        android.graphics.drawable.Drawable old = highlightImageView.getDrawable();
        highlightImageView.setImageBitmap(overlay);
        if (old instanceof BitmapDrawable) {
            Bitmap oldBitmap = ((BitmapDrawable) old).getBitmap();
            if (oldBitmap != null && oldBitmap != overlay && !oldBitmap.isRecycled()) oldBitmap.recycle();
        }
    }

    private void drawHighlightOverlay(int pageIndex) {
        if (highlightImageView == null || currentPageBitmap == null) return;

        boolean hasMatchOnPage = false;
        if (!searchResults.isEmpty()) {
            for (PdfTextSearchCore.SearchResult r : searchResults) {
                if (r.pageIndex == pageIndex) { hasMatchOnPage = true; break; }
            }
        }
        if (!hasMatchOnPage) {
            // Avoid allocating a full-page ARGB bitmap on every render when there is nothing to highlight.
            setHighlightBitmap(null);
            return;
        }

        Bitmap overlay = Bitmap.createBitmap(currentPageBitmap.getWidth(), currentPageBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        if (!searchResults.isEmpty() && currentSearchIndex >= 0 && currentSearchIndex < searchResults.size()) {
            PdfTextSearchCore.SearchResult current = searchResults.get(currentSearchIndex);
            for (int i = 0; i < searchResults.size(); i++) {
                PdfTextSearchCore.SearchResult r = searchResults.get(i);
                if (r.pageIndex != pageIndex) continue;
                paint.setColor(i == currentSearchIndex ? Color.parseColor("#AAFFC107") : Color.parseColor("#55FFF59D"));
                RectF rect = resultRect(r, currentPageBitmap);
                if (rect != null) {
                    canvas.drawRect(rect, paint);
                    if (i == currentSearchIndex) {
                        int targetY = Math.max(0, (int) rect.top - 120);
                        int targetX = Math.max(0, (int) rect.left - 60);
                        viewerVerticalScroll.post(() -> viewerVerticalScroll.scrollTo(targetX * 0, targetY));
                        viewerHorizontalScroll.post(() -> viewerHorizontalScroll.scrollTo(targetX, 0));
                    }
                }
            }
        }
        setHighlightBitmap(overlay);
    }

    private RectF resultRect(PdfTextSearchCore.SearchResult r, Bitmap bitmap) {
        int pw = rendererCore.getPageWidth();
        int ph = rendererCore.getPageHeight();
        if (pw <= 0 || ph <= 0 || r.y < 0 || r.y > ph) return null;

        int rotation = ((rendererCore.getRotationDegrees() % 360) + 360) % 360;
        boolean swapped = rotation == 90 || rotation == 270;
        // Coordinates from the text layer are relative to the unrotated page.
        int baseW = swapped ? bitmap.getHeight() : bitmap.getWidth();
        int baseH = swapped ? bitmap.getWidth() : bitmap.getHeight();
        float sx = baseW / (float) pw;
        float sy = baseH / (float) ph;
        float left = r.x * sx;
        float top = (ph - r.y - Math.max(r.fontSize, 8f)) * sy;
        float right = left + Math.max(8f, r.matched.length() * Math.max(r.fontSize, 8f) * 0.62f) * sx;
        float bottom = (ph - r.y) * sy;

        // Map the unrotated rectangle into the rendered (possibly rotated) bitmap.
        float outLeft = left, outTop = top, outRight = right, outBottom = bottom;
        if (rotation == 90) {
            outLeft = baseH - bottom; outRight = baseH - top;
            outTop = left; outBottom = right;
        } else if (rotation == 180) {
            outLeft = baseW - right; outRight = baseW - left;
            outTop = baseH - bottom; outBottom = baseH - top;
        } else if (rotation == 270) {
            outLeft = top; outRight = bottom;
            outTop = baseW - right; outBottom = baseW - left;
        }

        if (outRight < 0 || outBottom < 0 || outLeft > bitmap.getWidth() || outTop > bitmap.getHeight()) return null;
        return new RectF(Math.max(0, outLeft), Math.max(0, outTop),
                Math.min(bitmap.getWidth(), outRight), Math.min(bitmap.getHeight(), outBottom));
    }

    private void toggleNightMode() {
        rendererCore.setNightMode(!rendererCore.isNightMode());
        nightBtn.setIcon(rendererCore.isNightMode() ? "sun" : "moon",
                rendererCore.isNightMode() ? DesignSystem.COLOR_ACCENT_AMBER : DesignSystem.TEXT_SECONDARY);
        renderCurrentPage(rendererCore.getActivePageIndex());
    }

    private boolean isCurrentPageBookmarked() {
        if (activeFile == null) return false;
        return storageManager.isBookmarked(activeFile.getAbsolutePath(), rendererCore.getActivePageIndex());
    }

    private void toggleBookmarkCurrentPage() {
        if (activeFile == null) return;
        String path = activeFile.getAbsolutePath();
        int page = rendererCore.getActivePageIndex();
        if (storageManager.isBookmarked(path, page)) {
            storageManager.removeBookmark(path, page);
            bookmarkBtn.setIcon("star", DesignSystem.TEXT_SECONDARY);
            Toast.makeText(this, "Marcador eliminado de pág " + (page + 1), Toast.LENGTH_SHORT).show();
        } else {
            storageManager.addBookmark(path, page, "Página " + (page + 1));
            bookmarkBtn.setIcon("starfill", DesignSystem.COLOR_ACCENT_AMBER);
            Toast.makeText(this, "Página " + (page + 1) + " marcada como favorita", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPageJumpDialog() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        final EditText input = DesignSystem.createField(this,
                "Página (1 - " + rendererCore.getPageCount() + ")", false);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(rendererCore.getActivePageIndex() + 1));
        content.addView(input);

        DesignSystem.sheet(this, "Ir a página", content, "Ir", dialog -> {
            try {
                int target = Integer.parseInt(input.getText().toString().trim());
                if (target >= 1 && target <= rendererCore.getPageCount()) {
                    renderCurrentPage(target - 1);
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Número de página no válido", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Ingresa un número válido", Toast.LENGTH_SHORT).show();
            }
        }, "Cancelar").show();
    }

    /** Real in-document text search: extracts PDF text, lists matches and highlights them. */
    private void showTextSearchDialog() {
        if (activeFile == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText queryEdit = DesignSystem.createField(this, "Texto a buscar en el PDF", false);
        layout.addView(queryEdit);

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams buttonsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonsLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, 0);
        layout.addView(buttons, buttonsLp);

        Button searchBtn = DesignSystem.createPrimaryButton(this, "Buscar", null);
        buttons.addView(searchBtn, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View prevMatchBtn = DesignSystem.iconButton(this, "back", DesignSystem.TEXT_SECONDARY, null);
        View nextMatchBtn = DesignSystem.iconButton(this, "chevron", DesignSystem.TEXT_SECONDARY, null);
        LinearLayout.LayoutParams arrowLp = new LinearLayout.LayoutParams(
                DesignSystem.dp(this, DesignSystem.TOUCH_MIN), DesignSystem.dp(this, DesignSystem.TOUCH_MIN));
        arrowLp.setMargins(DesignSystem.dp(this, DesignSystem.SP_S), 0, 0, 0);
        buttons.addView(prevMatchBtn, arrowLp);
        buttons.addView(nextMatchBtn, new LinearLayout.LayoutParams(arrowLp));

        final TextView statusView = DesignSystem.text(this, "", DesignSystem.TEXT_CAPTION,
                DesignSystem.TEXT_SECONDARY, false);
        statusView.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, 0);
        layout.addView(statusView);
        updateSearchStatus(statusView);

        final android.app.Dialog dialog = DesignSystem.sheet(this, "Buscar en el documento",
                layout, "Cerrar", null, null);

        searchBtn.setOnClickListener(v -> {
            String query = queryEdit.getText().toString();
            if (query.trim().isEmpty()) {
                statusView.setText("Escribe un texto para buscar");
                return;
            }
            statusView.setText("Buscando…");
            final File file = activeFile;
            new Thread(() -> {
                try {
                    final List<PdfTextSearchCore.SearchResult> found = new PdfTextSearchCore().search(file, query);
                    runOnUiThread(() -> {
                        searchResults = found;
                        currentSearchIndex = found.isEmpty() ? -1 : 0;
                        updateSearchStatus(statusView);
                        if (!found.isEmpty()) {
                            jumpToSearchResult(0);
                        } else {
                            statusView.setText("Sin coincidencias (o el PDF no tiene capa de texto)");
                            Toast.makeText(this, "Sin coincidencias (o el PDF no tiene capa de texto)", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> statusView.setText("Error en la búsqueda: " + e.getMessage()));
                }
            }).start();
        });

        prevMatchBtn.setOnClickListener(v -> moveSearch(-1, statusView));
        nextMatchBtn.setOnClickListener(v -> moveSearch(1, statusView));
        dialog.show();
    }

    private LinearLayout.LayoutParams weightParams(float weight) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight);
    }

    private void updateSearchStatus(TextView statusView) {
        if (searchResults.isEmpty()) {
            statusView.setText("Sin resultados");
        } else {
            PdfTextSearchCore.SearchResult r = searchResults.get(currentSearchIndex);
            statusView.setText("Resultado " + (currentSearchIndex + 1) + " de " + searchResults.size() +
                    " · Página " + (r.pageIndex + 1) + " · \"" + r.matched + "\"");
        }
    }

    private void moveSearch(int delta, TextView statusView) {
        if (searchResults.isEmpty()) return;
        currentSearchIndex = (currentSearchIndex + delta + searchResults.size()) % searchResults.size();
        jumpToSearchResult(currentSearchIndex);
        updateSearchStatus(statusView);
    }

    private void jumpToSearchResult(int index) {
        if (index < 0 || index >= searchResults.size() || currentScreen != Screen.VIEWER) return;
        int page = searchResults.get(index).pageIndex;
        if (rendererCore.getActivePageIndex() != page) {
            renderCurrentPage(page);
        } else if (currentPageBitmap != null) {
            drawHighlightOverlay(page);
        }
    }

    private void showExtractRangeDialog() {
        if (activeFile == null) return;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView hint = DesignSystem.bodyText(this,
                "Elige el rango a extraer. El documento actual tiene "
                        + pageLabel(rendererCore.getPageCount()) + ".");
        layout.addView(hint);

        TextView startLabel = DesignSystem.fieldLabel(this, "PÁGINA INICIAL");
        startLabel.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_L), 0, DesignSystem.dp(this, DesignSystem.SP_S));
        layout.addView(startLabel);

        final EditText startEdit = DesignSystem.createField(this, "Ej: 1", false);
        startEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        startEdit.setText(String.valueOf(rendererCore.getActivePageIndex() + 1));
        layout.addView(startEdit);

        TextView endLabel = DesignSystem.fieldLabel(this, "PÁGINA FINAL");
        endLabel.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, DesignSystem.dp(this, DesignSystem.SP_S));
        layout.addView(endLabel);

        final EditText endEdit = DesignSystem.createField(this,
                "Ej: " + rendererCore.getPageCount(), false);
        endEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        endEdit.setText(String.valueOf(rendererCore.getActivePageIndex() + 1));
        layout.addView(endEdit);

        DesignSystem.sheet(this, "Extraer páginas", layout, "Extraer", dialog -> {
            try {
                int total = rendererCore.getPageCount();
                int start = Integer.parseInt(startEdit.getText().toString().trim()) - 1;
                int end = Integer.parseInt(endEdit.getText().toString().trim()) - 1;
                if (start < 0 || end < 0 || start >= total || end >= total) {
                    Toast.makeText(this, "Rango no válido (1 - " + total + ")", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (start > end) { int tmp = start; start = end; end = tmp; }
                File extracted = pageManagerCore.extractPageRange(activeFile, start, end);
                String name = "Extraído (Pág " + (start + 1) + "-" + (end + 1) + ")";
                storageManager.registerDocument(name, extracted.getAbsolutePath(), "", extracted.length(),
                        end - start + 1, true);
                dialog.dismiss();
                openPdfFile(extracted, name);
                Toast.makeText(this, "¡Páginas extraídas con éxito! (" + (end - start + 1) + " pág.)",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error al extraer: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, "Cancelar").show();
    }

    private void confirmDeleteCurrentPage() {
        if (activeFile == null) return;
        if (rendererCore.getPageCount() <= 1) {
            Toast.makeText(this, "No se puede eliminar la única página del documento", Toast.LENGTH_SHORT).show();
            return;
        }

        final int pageNum = rendererCore.getActivePageIndex() + 1;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(DesignSystem.bodyText(this,
                "Se eliminará la página " + pageNum + " del documento actual. Se creará una nueva versión limpia y el original no se modifica."));

        DesignSystem.sheet(this, "Eliminar página", layout, "Eliminar", dialog -> {
            try {
                File result = pageManagerCore.deletePage(activeFile, rendererCore.getActivePageIndex());
                storageManager.registerDocument(activeTitle + " (Sin pág " + pageNum + ")",
                        result.getAbsolutePath(), "", result.length(), rendererCore.getPageCount() - 1, true);
                dialog.dismiss();
                openPdfFile(result, activeTitle + " (Pág eliminada)");
                Toast.makeText(this, "Página eliminada con éxito", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }, "Cancelar", true).show();
    }

    private void compressActiveDocument() {
        if (activeFile == null) return;
        try {
            long origSize = activeFile.length();
            int sourcePages = rendererCore.getPageCount();

            File compressed = pageManagerCore.compressPdf(activeFile, 82);
            int writtenPages = pageManagerCore.getLastCompressionPages();

            // Second pass with lower quality only if the first one did not help.
            if (compressed.length() >= origSize) {
                File stronger = pageManagerCore.compressPdf(activeFile, 65);
                if (stronger.length() < compressed.length()) {
                    compressed.delete();
                    compressed = stronger;
                    writtenPages = pageManagerCore.getLastCompressionPages();
                } else {
                    stronger.delete();
                }
            }

            long newSize = compressed.length();
            if (newSize >= origSize || writtenPages != sourcePages) {
                compressed.delete();
                Toast.makeText(this, "No se logró reducir el tamaño sin perder páginas; se conserva el original (" +
                        (origSize / 1024) + " KB)", Toast.LENGTH_LONG).show();
                return;
            }

            long savings = origSize - newSize;
            int percent = (int) ((savings * 100) / origSize);
            storageManager.registerDocument(activeTitle + " (Optimizado)", compressed.getAbsolutePath(), "", newSize, writtenPages, true);
            openPdfFile(compressed, activeTitle + " (Comprimido)");
            Toast.makeText(this, "Compresión completa: " + origSize / 1024 + " KB → " + newSize / 1024 +
                    " KB (" + percent + "% menos, " + writtenPages + " páginas)", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al comprimir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // 6b. MERGE SCREEN (multi-select + ordering + page verification)
    // ==========================================
    private void showMergeScreen() {
        currentScreen = Screen.MERGE;
        releaseViewerBitmaps();
        rootLayout.removeAllViews();

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(DesignSystem.BG_APP);
        shell.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(DesignSystem.SURFACE_HEADER);
        header.setPadding(DesignSystem.dp(this, 6), DesignSystem.dp(this, 8),
                DesignSystem.dp(this, DesignSystem.SP_L), DesignSystem.dp(this, 8));
        header.addView(DesignSystem.iconButton(this, "back", DesignSystem.TEXT_PRIMARY,
                v -> showMainScreen()));

        TextView title = DesignSystem.text(this, "Unir PDFs", DesignSystem.TEXT_TITLE,
                DesignSystem.TEXT_PRIMARY, true);
        title.setPadding(DesignSystem.dp(this, DesignSystem.SP_S), 0, 0, 0);
        header.addView(title, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        shell.addView(header);

        ScrollView scroll = DesignSystem.screenScroll(this);
        LinearLayout content = DesignSystem.screenColumn(this);

        content.addView(DesignSystem.bodyText(this,
                "Marca al menos dos documentos. Usa las flechas para elegir el orden en que se unirán las páginas."));

        Button importBtn = DesignSystem.createSecondaryButton(this, "Importar otro PDF",
                v -> openPdfPickerForMerge());
        LinearLayout.LayoutParams importLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        importLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_L), 0, DesignSystem.dp(this, DesignSystem.SP_S));
        content.addView(importBtn, importLp);

        mergeListContainer = new LinearLayout(this);
        mergeListContainer.setOrientation(LinearLayout.VERTICAL);
        mergeListContainer.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_S), 0, 0);
        content.addView(mergeListContainer);
        rebuildMergeList();

        Button mergeBtn = DesignSystem.createPrimaryButton(this, "Unir seleccionados", v -> performMerge());
        LinearLayout.LayoutParams mergeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mergeLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_L), 0, 0);
        content.addView(mergeBtn, mergeLp);

        scroll.addView(content);
        shell.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        rootLayout.addView(shell);
    }

    private void rebuildMergeList() {
        if (mergeListContainer == null) return;
        mergeListContainer.removeAllViews();
        mergeDocs = storageManager.getRecentDocuments();

        List<String> validOrder = new ArrayList<>();
        for (PdfDocumentItem item : mergeDocs) {
            if (mergeOrder.contains(item.getId())) validOrder.add(item.getId());
        }
        mergeOrder = validOrder;

        if (mergeDocs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No hay documentos. Importa dos PDF para poder unirlos.");
            empty.setTextColor(DesignSystem.TEXT_SECONDARY);
            mergeListContainer.addView(empty);
            return;
        }
        for (PdfDocumentItem item : mergeDocs) mergeListContainer.addView(buildMergeRow(item));
    }

    private View buildMergeRow(PdfDocumentItem item) {
        int position = mergeOrder.indexOf(item.getId());
        boolean selected = position >= 0;

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(DesignSystem.createClickableCardDrawable(this,
                selected ? DesignSystem.COLOR_PRIMARY_TINT : DesignSystem.SURFACE_CARD,
                DesignSystem.SURFACE_CARD_PRESSED, DesignSystem.RADIUS_MD));
        row.setPadding(DesignSystem.dp(this, DesignSystem.SP_M), DesignSystem.dp(this, DesignSystem.SP_M),
                DesignSystem.dp(this, DesignSystem.SP_S), DesignSystem.dp(this, DesignSystem.SP_M));
        row.setMinimumHeight(DesignSystem.dp(this, 60));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, DesignSystem.dp(this, DesignSystem.SP_S));
        row.setLayoutParams(lp);

        View check = Icons.view(this, selected ? "select" : "unselect", 20,
                selected ? DesignSystem.COLOR_PRIMARY : DesignSystem.TEXT_TERTIARY);
        LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(
                DesignSystem.dp(this, 20), DesignSystem.dp(this, 20));
        checkLp.setMargins(0, 0, DesignSystem.dp(this, DesignSystem.SP_M), 0);
        row.addView(check, checkLp);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        textCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView name = DesignSystem.text(this, item.getTitle(), 14,
                DesignSystem.TEXT_PRIMARY, selected);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        textCol.addView(name);

        TextView meta = DesignSystem.text(this,
                pageLabel(item.getPageCount()) + " · " + Math.max(1, item.getSizeBytes() / 1024) + " KB",
                DesignSystem.TEXT_MICRO, DesignSystem.TEXT_SECONDARY, false);
        textCol.addView(meta);
        row.addView(textCol);

        if (selected) {
            TextView order = DesignSystem.text(this, String.valueOf(position + 1), 12,
                    DesignSystem.COLOR_PRIMARY, true);
            order.setGravity(Gravity.CENTER);
            order.setBackground(DesignSystem.createCardDrawable(this,
                    DesignSystem.withAlpha(DesignSystem.COLOR_PRIMARY, 0.18f), 0, 8));
            LinearLayout.LayoutParams orderLp = new LinearLayout.LayoutParams(
                    DesignSystem.dp(this, 24), DesignSystem.dp(this, 24));
            orderLp.setMargins(0, 0, DesignSystem.dp(this, 6), 0);
            row.addView(order, orderLp);
        }

        View up = DesignSystem.iconButton(this, "up", DesignSystem.TEXT_SECONDARY,
                v -> moveMergeItem(item.getId(), -1));
        up.setMinimumWidth(DesignSystem.dp(this, 36));
        up.setMinimumHeight(DesignSystem.dp(this, 36));
        row.addView(up, new LinearLayout.LayoutParams(
                DesignSystem.dp(this, 36), DesignSystem.dp(this, 36)));

        View down = DesignSystem.iconButton(this, "down", DesignSystem.TEXT_SECONDARY,
                v -> moveMergeItem(item.getId(), 1));
        down.setMinimumWidth(DesignSystem.dp(this, 36));
        down.setMinimumHeight(DesignSystem.dp(this, 36));
        row.addView(down, new LinearLayout.LayoutParams(
                DesignSystem.dp(this, 36), DesignSystem.dp(this, 36)));

        row.setOnClickListener(v -> {
            if (mergeOrder.contains(item.getId())) mergeOrder.remove(item.getId());
            else mergeOrder.add(item.getId());
            rebuildMergeList();
        });
        return row;
    }

    private void moveMergeItem(String id, int delta) {
        int from = mergeOrder.indexOf(id);
        if (from < 0) {
            Toast.makeText(this, "Primero marca este documento con ☑", Toast.LENGTH_SHORT).show();
            return;
        }
        int to = from + delta;
        if (to < 0 || to >= mergeOrder.size()) return;
        String other = mergeOrder.get(to);
        mergeOrder.set(to, id);
        mergeOrder.set(from, other);
        rebuildMergeList();
    }

    private void performMerge() {
        if (mergeOrder.size() < 2) {
            Toast.makeText(this, "Marca al menos dos documentos", Toast.LENGTH_SHORT).show();
            return;
        }
        List<File> files = new ArrayList<>();
        int expectedPages = 0;
        for (String id : mergeOrder) {
            PdfDocumentItem item = null;
            for (PdfDocumentItem d : mergeDocs) if (d.getId().equals(id)) { item = d; break; }
            if (item == null) continue;
            File f = new File(item.getPath());
            if (!f.exists()) {
                Toast.makeText(this, "Falta el archivo: " + item.getTitle(), Toast.LENGTH_LONG).show();
                return;
            }
            files.add(f);
            expectedPages += item.getPageCount();
        }

        try {
            File merged = pageManagerCore.mergePdfs(files);
            int actualPages = new PdfTextSearchCore().countPages(merged);
            if (actualPages != expectedPages) {
                merged.delete();
                Toast.makeText(this, "Error: se esperaban " + expectedPages + " páginas y se generaron " + actualPages, Toast.LENGTH_LONG).show();
                return;
            }
            String name = "Combinado (" + actualPages + " páginas)";
            storageManager.registerDocument(name, merged.getAbsolutePath(), "", merged.length(), actualPages, true);
            openPdfFile(merged, name);
            Toast.makeText(this, "Unión completada: " + actualPages + " páginas de " + files.size() + " documentos", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al unir: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdfPickerForMerge() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, REQUEST_MERGE_IMPORT);
    }

    private boolean handleIncomingPdfForMerge(Uri uri) {
        File imported = storageManager.importUriToStorage(uri,
                "documento_" + System.currentTimeMillis() + "_" + Math.abs(uri.hashCode()) + ".pdf");
        if (imported != null && imported.exists()) {
            storageManager.registerDocument("Documento Importado", imported.getAbsolutePath(), uri.toString(), imported.length(),
                    Math.max(1, new PdfTextSearchCore().countPages(imported)), false);
            return true;
        }
        Toast.makeText(this, "No se pudo importar el PDF", Toast.LENGTH_SHORT).show();
        return false;
    }

    // ==========================================
    // 7. DRAWING, ANNOTATION & SIGNATURE MODAL
    // ==========================================
    private void openAnnotationModal() {
        if (activeFile == null) return;

        LinearLayout modalLayout = new LinearLayout(this);
        modalLayout.setOrientation(LinearLayout.VERTICAL);

        final PdfAnnotationCore.DrawingCanvasView canvasView = new PdfAnnotationCore.DrawingCanvasView(this);
        canvasView.setBackgroundColor(Color.WHITE);

        LinearLayout toolBar = new LinearLayout(this);
        toolBar.setOrientation(LinearLayout.HORIZONTAL);
        toolBar.setGravity(Gravity.CENTER_VERTICAL);

        toolBar.addView(toolChip("pen", "Bolígrafo",
                v -> canvasView.init(Color.parseColor("#1E40AF"), 6f, false)));
        toolBar.addView(toolChip("edit", "Resaltador",
                v -> canvasView.init(Color.parseColor("#EAB308"), 8f, true)));
        toolBar.addView(toolChip("select", "Firma",
                v -> canvasView.init(Color.BLACK, 5f, false)));
        toolBar.addView(toolChip("trash", "Limpiar", v -> canvasView.clear()));

        modalLayout.addView(toolBar);
        LinearLayout.LayoutParams canvasLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, DesignSystem.dp(this, 300));
        canvasLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_M), 0, 0);
        modalLayout.addView(canvasView, canvasLp);

        TextView hint = DesignSystem.text(this, "Dibuja o firma sobre la página actual y guarda para incrustarlo en el PDF.",
                DesignSystem.TEXT_MICRO, DesignSystem.TEXT_TERTIARY, false);
        hint.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_S), 0, 0);
        modalLayout.addView(hint);

        DesignSystem.sheet(this, "Anotar y firmar", modalLayout, "Guardar en PDF", dialog -> {
            Bitmap drawn = canvasView.getDrawnBitmap();
            if (drawn != null) {
                try {
                    File annotated = annotationCore.bakeAnnotationToPdf(activeFile,
                            rendererCore.getActivePageIndex(), drawn);
                    storageManager.registerDocument(activeTitle + " (Anotado)", annotated.getAbsolutePath(),
                            "", annotated.length(), rendererCore.getPageCount(), true);
                    dialog.dismiss();
                    openPdfFile(annotated, activeTitle + " (Anotado)");
                    Toast.makeText(this, "¡Anotación y firma guardadas en el PDF!", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error al guardar anotación: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, "Cancelar").show();
    }

    /** Compact icon + text control used inside sheets. */
    private View toolChip(String icon, String label, View.OnClickListener onClick) {
        LinearLayout chip = new LinearLayout(this);
        chip.setOrientation(LinearLayout.HORIZONTAL);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(DesignSystem.createClickableCardDrawable(this,
                DesignSystem.SURFACE_ELEVATED, DesignSystem.SURFACE_CARD_PRESSED, DesignSystem.RADIUS_SM));
        chip.setPadding(DesignSystem.dp(this, DesignSystem.SP_S), DesignSystem.dp(this, 6),
                DesignSystem.dp(this, DesignSystem.SP_S), DesignSystem.dp(this, 6));
        chip.setClickable(true);
        chip.setMinimumHeight(DesignSystem.dp(this, 40));

        chip.addView(Icons.view(this, icon, 15, DesignSystem.TEXT_SECONDARY));
        TextView t = DesignSystem.text(this, label, 11, DesignSystem.TEXT_PRIMARY, false);
        t.setPadding(DesignSystem.dp(this, 5), 0, 0, 0);
        chip.addView(t);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, DesignSystem.dp(this, 6), 0);
        chip.setLayoutParams(lp);
        chip.setOnClickListener(onClick);
        return chip;
    }

    private void shareCurrentDocument() {
        if (activeFile != null && activeFile.exists()) {
            Uri uri = DocumentFileStore.contentUri(this, activeFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            DocumentFileStore.grantRead(this, shareIntent, uri);
            startActivity(Intent.createChooser(shareIntent, "Compartir PDF"));
        } else {
            Toast.makeText(this, "No hay archivo activo para compartir", Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // 8. CREATOR SCREEN
    // ==========================================
    private void showCreatorScreen() {
        currentScreen = Screen.CREATOR;
        releaseViewerBitmaps();
        rootLayout.removeAllViews();

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(DesignSystem.BG_APP);
        shell.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(DesignSystem.SURFACE_HEADER);
        header.setPadding(DesignSystem.dp(this, 6), DesignSystem.dp(this, 8),
                DesignSystem.dp(this, DesignSystem.SP_L), DesignSystem.dp(this, 8));
        header.addView(DesignSystem.iconButton(this, "back", DesignSystem.TEXT_PRIMARY,
                v -> showMainScreen()));

        TextView headerTitle = DesignSystem.text(this, "Nuevo documento", DesignSystem.TEXT_TITLE,
                DesignSystem.TEXT_PRIMARY, true);
        headerTitle.setPadding(DesignSystem.dp(this, DesignSystem.SP_S), 0, 0, 0);
        header.addView(headerTitle, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        shell.addView(header);

        ScrollView scroll = DesignSystem.screenScroll(this);
        LinearLayout form = DesignSystem.screenColumn(this);

        form.addView(DesignSystem.bodyText(this,
                "Redacta el contenido y PDF Studio lo maqueta en una página A4 lista para compartir."));

        TextView lTitle = DesignSystem.fieldLabel(this, "TÍTULO");
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_XL), 0, 0);
        form.addView(lTitle, labelLp);

        EditText editTitle = DesignSystem.createField(this, "Ej: Minuta de reunión", false);
        form.addView(editTitle);

        TextView lBody = DesignSystem.fieldLabel(this, "CONTENIDO");
        lBody.setPadding(0, DesignSystem.dp(this, DesignSystem.SP_L), 0, DesignSystem.dp(this, DesignSystem.SP_S));
        form.addView(lBody);

        EditText editBody = DesignSystem.createField(this, "Escribe aquí el contenido del PDF…", true);
        form.addView(editBody);

        Button genBtn = DesignSystem.createPrimaryButton(this, "Generar PDF", v -> {
            String t = editTitle.getText().toString().trim();
            String c = editBody.getText().toString().trim();
            if (t.isEmpty()) t = "Documento sin título";
            if (c.isEmpty()) {
                Toast.makeText(this, "Escribe algo de contenido primero", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                File created = creatorCore.createPdfFromText(t, c);
                storageManager.registerDocument(t, created.getAbsolutePath(), "", created.length(), 1, true);
                openPdfFile(created, t);
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, DesignSystem.dp(this, DesignSystem.SP_XL), 0, 0);
        form.addView(genBtn, btnLp);

        scroll.addView(form);
        shell.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        rootLayout.addView(shell);
    }

    // ==========================================
    // 9. FILE PICKERS, CAMERA CAPTURE & DEMO
    // ==========================================
    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_PICK_PDF);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    private void openCameraCapture() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        launchCameraIntent();
    }

    private void launchCameraIntent() {
        try {
            cameraOutputFile = DocumentFileStore.newFile(this, "Camara", ".jpg");
            Uri outputUri = DocumentFileStore.contentUri(this, cameraOutputFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            DocumentFileStore.grantReadWrite(this, intent, outputUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CAPTURE_CAMERA);
            } else {
                Toast.makeText(this, "Cámara no disponible, usando selector de galería", Toast.LENGTH_SHORT).show();
                openImagePicker();
            }
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo preparar la cámara: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_MERGE_IMPORT) {
            if (resultCode == RESULT_OK && data != null) {
                int imported = 0;
                android.content.ClipData clip = data.getClipData();
                if (clip != null) {
                    for (int i = 0; i < clip.getItemCount(); i++) {
                        if (handleIncomingPdfForMerge(clip.getItemAt(i).getUri())) imported++;
                    }
                } else if (data.getData() != null) {
                    if (handleIncomingPdfForMerge(data.getData())) imported++;
                }
                if (imported > 1) {
                    Toast.makeText(this, imported + " PDFs importados. Márcalos para unirlos.",
                            Toast.LENGTH_LONG).show();
                }
            }
            showMergeScreen();
            return;
        }
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_PICK_PDF && data != null && data.getData() != null) {
            handleIncomingPdf(data.getData());
        } else if (requestCode == REQUEST_PICK_IMAGE && data != null && data.getData() != null) {
            try {
                File created = creatorCore.convertImageToPdf(data.getData());
                if (created != null) {
                    storageManager.registerDocument("Foto a PDF", created.getAbsolutePath(), "", created.length(), 1, true);
                    openPdfFile(created, "Foto a PDF");
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CAPTURE_CAMERA) {
            if (cameraOutputFile != null && cameraOutputFile.exists() && cameraOutputFile.length() > 1000) {
                buildPdfFromCameraFile(cameraOutputFile, false);
            } else if (data != null && data.getExtras() != null) {
                Bitmap thumb = (Bitmap) data.getExtras().get("data");
                if (thumb != null) {
                    try {
                        File tempImg = DocumentFileStore.newFile(this, "CamaraMiniatura", ".jpg");
                        java.io.FileOutputStream out = new java.io.FileOutputStream(tempImg);
                        thumb.compress(Bitmap.CompressFormat.JPEG, 90, out);
                        out.close();
                        buildPdfFromCameraFile(tempImg, true);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al guardar la foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se capturó ninguna imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void buildPdfFromCameraFile(File imageFile, boolean temporary) {
        try {
            File created = creatorCore.convertImageToPdf(DocumentFileStore.contentUri(this, imageFile));
            if (created == null) {
                Toast.makeText(this, "No se pudo leer la imagen capturada", Toast.LENGTH_SHORT).show();
                return;
            }
            storageManager.registerDocument("Escaneo de Cámara", created.getAbsolutePath(), "", created.length(), 1, true);
            openPdfFile(created, "Escaneo de Cámara");
            Toast.makeText(this, "¡Documento escaneado y convertido a PDF!", Toast.LENGTH_SHORT).show();
            if (temporary && imageFile.exists()) imageFile.delete();
        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void handleIncomingPdf(Uri uri) {
        File imported = storageManager.importUriToStorage(uri, "documento_" + System.currentTimeMillis() + ".pdf");
        if (imported != null && imported.exists()) {
            if (rendererCore.open(imported)) {
                storageManager.registerDocument("Documento Importado", imported.getAbsolutePath(), uri.toString(), imported.length(), rendererCore.getPageCount());
                showViewerScreen("Documento Importado");
            }
        } else {
            Toast.makeText(this, "No se pudo leer el archivo seleccionado", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPdfFile(File file, String title) {
        if (rendererCore.open(file)) {
            activeFile = file;
            showViewerScreen(title);
        } else {
            Toast.makeText(this, "Error al renderizar archivo PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDemoDocument() {
        try {
            File demo = creatorCore.createPdfFromText(
                    "PDF Studio v3.3 - Documento de prueba",
                    "Este PDF de ejemplo se ha generado localmente para probar el visor, la búsqueda de texto y las herramientas de página.\n\n" +
                    "Incluye: navegación y zoom, modo noche, marcadores, buscador con resaltado, extracción de rangos, unión de varios PDF, compresión JPEG y anotaciones.\n\n" +
                    "Busca la palabra 'ejemplo' con el botón de lupa para comprobar la búsqueda real dentro del contenido."
            );
            storageManager.registerDocument("Guía Interactiva v3.3", demo.getAbsolutePath(), "", demo.length(), 1, true);
            openPdfFile(demo, "Documento Demo v3.3");
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rendererCore.close();
    }
}
