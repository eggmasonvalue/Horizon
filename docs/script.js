document.addEventListener('DOMContentLoaded', () => {
    initClock();
    initThemeGallery();
    initHeroSimulation();
    renderMiniMacroGrid();
    renderMicroNoTomorrow();
    renderMicroVsYesterday();
    renderMicroCustom();
    renderMicroHealth();
    updateTodayRotationBadge();
});

const THEMES = {
    sage_garden: {
        id: 'sage_garden',
        name: 'Iconic',
        category: 'Curated',
        isRotatable: true,
        light: {
            bg: '#EAE7DC',
            past: '#8E8D8A',
            current: '#E85A4F',
            future: 'rgba(142, 141, 138, 0.30)',
            futureBorder: 'rgba(142, 141, 138, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#191816',
            past: '#8E8D8A',
            current: '#E85A4F',
            future: 'rgba(142, 141, 138, 0.30)',
            futureBorder: 'rgba(142, 141, 138, 0.45)',
            text: '#FAF8F5'
        }
    },
    nordic_minimal: {
        id: 'nordic_minimal',
        name: 'Nordic Minimal',
        category: 'Curated',
        isRotatable: true,
        light: {
            bg: '#F8F9FA',
            past: '#7D9D9C',
            current: '#E76F51',
            future: 'rgba(125, 157, 156, 0.30)',
            futureBorder: 'rgba(125, 157, 156, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#131A19',
            past: '#6F9998',
            current: '#E76F51',
            future: 'rgba(111, 153, 152, 0.30)',
            futureBorder: 'rgba(111, 153, 152, 0.45)',
            text: '#FAF8F5'
        }
    },
    warm_sand: {
        id: 'warm_sand',
        name: 'Warm Sand',
        category: 'Curated',
        isRotatable: true,
        light: {
            bg: '#FAF7F2',
            past: '#C9ADA7',
            current: '#9A8873',
            future: 'rgba(201, 173, 167, 0.30)',
            futureBorder: 'rgba(201, 173, 167, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#171311',
            past: '#A89690',
            current: '#D4B896',
            future: 'rgba(168, 150, 144, 0.30)',
            futureBorder: 'rgba(168, 150, 144, 0.45)',
            text: '#FAF8F5'
        }
    },
    glacial_peak: {
        id: 'glacial_peak',
        name: 'Glacial Peak',
        category: 'Curated',
        isRotatable: true,
        light: {
            bg: '#F5F8FA',
            past: '#7D94B0',
            current: '#E57373',
            future: 'rgba(125, 148, 176, 0.30)',
            futureBorder: 'rgba(125, 148, 176, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#0B1120',
            past: '#5B9AA0',
            current: '#FFA07A',
            future: 'rgba(91, 154, 160, 0.30)',
            futureBorder: 'rgba(91, 154, 160, 0.45)',
            text: '#FAF8F5'
        }
    },
    rose_quartz: {
        id: 'rose_quartz',
        name: 'Rose Quartz',
        category: 'Curated',
        isRotatable: true,
        light: {
            bg: '#FFF9FA',
            past: '#C5A3AB',
            current: '#6D9DC5',
            future: 'rgba(197, 163, 171, 0.30)',
            futureBorder: 'rgba(197, 163, 171, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#1B1725',
            past: '#A78BAA',
            current: '#F2C14E',
            future: 'rgba(167, 139, 170, 0.30)',
            futureBorder: 'rgba(167, 139, 170, 0.45)',
            text: '#FAF8F5'
        }
    },
    monochrome_zen: {
        id: 'monochrome_zen',
        name: 'Monochrome Zen',
        category: 'Curated',
        isRotatable: true,
        light: {
            bg: '#FAFAFA',
            past: '#666666',
            current: '#000000',
            future: 'rgba(102, 102, 102, 0.30)',
            futureBorder: 'rgba(102, 102, 102, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#121212',
            past: '#888888',
            current: '#FFFFFF',
            future: 'rgba(136, 136, 136, 0.30)',
            futureBorder: 'rgba(136, 136, 136, 0.45)',
            text: '#FAF8F5'
        }
    },
    health_steps: {
        id: 'health_steps',
        name: 'Steps Green',
        category: 'Health Connect',
        isRotatable: false,
        light: {
            bg: '#F1F8E9',
            past: '#AED581',
            current: '#558B2F',
            future: 'rgba(174, 213, 129, 0.30)',
            futureBorder: 'rgba(174, 213, 129, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#152011',
            past: '#558B2F',
            current: '#8BC34A',
            future: 'rgba(85, 139, 47, 0.30)',
            futureBorder: 'rgba(85, 139, 47, 0.45)',
            text: '#FAF8F5'
        }
    },
    health_calories: {
        id: 'health_calories',
        name: 'Vitality Orange',
        category: 'Health Connect',
        isRotatable: false,
        light: {
            bg: '#FFF3E0',
            past: '#FFB74D',
            current: '#E65100',
            future: 'rgba(255, 183, 77, 0.30)',
            futureBorder: 'rgba(255, 183, 77, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#22140A',
            past: '#E65100',
            current: '#FF9800',
            future: 'rgba(230, 81, 0, 0.30)',
            futureBorder: 'rgba(230, 81, 0, 0.45)',
            text: '#FAF8F5'
        }
    },
    health_distance: {
        id: 'health_distance',
        name: 'Distance Purple',
        category: 'Health Connect',
        isRotatable: false,
        light: {
            bg: '#F3E5F5',
            past: '#BA68C8',
            current: '#6A1B9A',
            future: 'rgba(186, 104, 200, 0.30)',
            futureBorder: 'rgba(186, 104, 200, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#1C0F24',
            past: '#8E24AA',
            current: '#BA68C8',
            future: 'rgba(142, 36, 170, 0.30)',
            futureBorder: 'rgba(142, 36, 170, 0.45)',
            text: '#FAF8F5'
        }
    },
    health_sleep: {
        id: 'health_sleep',
        name: 'Deep Sleep Blue',
        category: 'Health Connect',
        isRotatable: false,
        light: {
            bg: '#E3F2FD',
            past: '#64B5F6',
            current: '#1565C0',
            future: 'rgba(100, 181, 246, 0.30)',
            futureBorder: 'rgba(100, 181, 246, 0.45)',
            text: '#1F2022'
        },
        dark: {
            bg: '#0A1424',
            past: '#1976D2',
            current: '#64B5F6',
            future: 'rgba(25, 118, 210, 0.30)',
            futureBorder: 'rgba(25, 118, 210, 0.45)',
            text: '#FAF8F5'
        }
    }
};

const SHAPES = [
    { id: 'shape-rounded', label: 'Rounded Square' },
    { id: 'shape-circle', label: 'Circle' },
    { id: 'shape-rhombus', label: 'Rhombus' }
];

let simState = {
    themeId: 'sage_garden',
    isDark: false,
    shapeIndex: 0,
    isDailyRotation: false
};

function getRotatedThemeId(date = new Date()) {
    const rotatableKeys = Object.keys(THEMES).filter(k => THEMES[k].isRotatable);
    const epochDay = Math.floor(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()) / (1000 * 60 * 60 * 24));
    const dayIndex = ((epochDay % rotatableKeys.length) + rotatableKeys.length) % rotatableKeys.length;
    return rotatableKeys[dayIndex];
}

function initClock() {
    const clockEl = document.getElementById('sim-clock');
    if (!clockEl) return;
    const updateTime = () => {
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        clockEl.textContent = `${hours}:${minutes}`;
    };
    updateTime();
    setInterval(updateTime, 30000);
}

function initHeroSimulation() {
    const gridContainer = document.getElementById('life-grid');
    if (!gridContainer) return;

    gridContainer.innerHTML = '';
    const totalYears = 90;
    const yearsLived = 26;
    const cols = 7;

    gridContainer.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;

    for (let i = 0; i < totalYears; i++) {
        const dot = document.createElement('div');
        dot.classList.add('year-dot', SHAPES[simState.shapeIndex].id);

        if (i < yearsLived) {
            dot.classList.add('past');
        } else if (i === yearsLived) {
            dot.classList.add('current');
            dot.title = "Current Year";
        } else {
            dot.classList.add('future');
        }

        gridContainer.appendChild(dot);
    }

    applyHeroTheme();

    // Theme Pills Click Handler
    const themePillButtons = document.querySelectorAll('#hero-theme-pills .theme-pill');
    themePillButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const themeId = btn.getAttribute('data-theme');
            setHeroTheme(themeId, false);
        });
    });

    // Dark Mode Toggle
    const darkToggleBtn = document.getElementById('toggle-dark-mode');
    if (darkToggleBtn) {
        darkToggleBtn.addEventListener('click', () => {
            simState.isDark = !simState.isDark;
            updateDarkModeButton();
            applyHeroTheme();
        });
    }

    // Daily Rotation Toggle
    const rotationToggleBtn = document.getElementById('toggle-daily-rotation');
    if (rotationToggleBtn) {
        rotationToggleBtn.addEventListener('click', () => {
            simState.isDailyRotation = !simState.isDailyRotation;
            updateRotationButton();
            if (simState.isDailyRotation) {
                const todayThemeId = getRotatedThemeId();
                setHeroTheme(todayThemeId, true);
            }
        });
    }

    // Shape Cycle Toggle
    const shapeToggleBtn = document.getElementById('toggle-shape');
    if (shapeToggleBtn) {
        shapeToggleBtn.addEventListener('click', () => {
            simState.shapeIndex = (simState.shapeIndex + 1) % SHAPES.length;
            updateShapeClasses();
            const shapeLabel = document.getElementById('shape-label');
            if (shapeLabel) {
                shapeLabel.textContent = SHAPES[simState.shapeIndex].label;
            }
        });
    }
}

function setHeroTheme(themeId, fromRotation = false) {
    if (!THEMES[themeId]) return;
    simState.themeId = themeId;
    if (!fromRotation && simState.isDailyRotation) {
        simState.isDailyRotation = false;
        updateRotationButton();
    }
    updateThemePillsUI();
    applyHeroTheme();
}

function updateThemePillsUI() {
    const themePillButtons = document.querySelectorAll('#hero-theme-pills .theme-pill');
    themePillButtons.forEach(btn => {
        if (btn.getAttribute('data-theme') === simState.themeId) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

function updateDarkModeButton() {
    const darkToggleBtn = document.getElementById('toggle-dark-mode');
    const darkIcon = document.getElementById('dark-mode-icon');
    const darkLabel = document.getElementById('dark-mode-label');
    if (!darkToggleBtn) return;

    if (simState.isDark) {
        darkToggleBtn.classList.add('active');
        if (darkIcon) darkIcon.className = 'fa-solid fa-sun';
        if (darkLabel) darkLabel.textContent = 'Light Mode';
    } else {
        darkToggleBtn.classList.remove('active');
        if (darkIcon) darkIcon.className = 'fa-solid fa-moon';
        if (darkLabel) darkLabel.textContent = 'Dark Mode';
    }
}

function updateRotationButton() {
    const rotationToggleBtn = document.getElementById('toggle-daily-rotation');
    const rotationLabel = document.getElementById('rotation-label');
    if (!rotationToggleBtn) return;

    if (simState.isDailyRotation) {
        rotationToggleBtn.classList.add('active');
        if (rotationLabel) rotationLabel.textContent = 'Daily Rotation: On';
    } else {
        rotationToggleBtn.classList.remove('active');
        if (rotationLabel) rotationLabel.textContent = 'Daily Rotation: Off';
    }
}

function updateShapeClasses() {
    const dots = document.querySelectorAll('#life-grid .year-dot');
    dots.forEach(dot => {
        SHAPES.forEach(s => dot.classList.remove(s.id));
        dot.classList.add(SHAPES[simState.shapeIndex].id);
    });
}

function applyHeroTheme() {
    const simBox = document.getElementById('macro-simulation');
    if (!simBox) return;

    const theme = THEMES[simState.themeId] || THEMES.sage_garden;
    const variant = simState.isDark ? theme.dark : theme.light;

    simBox.style.backgroundColor = variant.bg;
    simBox.style.borderColor = simState.isDark ? '#333' : '#1a1a1a';

    const header = simBox.querySelector('.sim-phone-header');
    if (header) {
        header.style.color = variant.text;
    }

    const caption = simBox.querySelector('.caption');
    if (caption) {
        caption.style.color = simState.isDark ? 'rgba(255, 255, 255, 0.6)' : 'rgba(0, 0, 0, 0.55)';
    }

    const notch = simBox.querySelector('.sim-notch');
    if (notch) {
        notch.style.backgroundColor = simState.isDark ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.2)';
    }

    const pastDots = simBox.querySelectorAll('.year-dot.past');
    pastDots.forEach(dot => {
        dot.style.backgroundColor = variant.past;
        dot.style.borderColor = variant.past;
    });

    const currentDots = simBox.querySelectorAll('.year-dot.current');
    currentDots.forEach(dot => {
        dot.style.backgroundColor = variant.current;
        dot.style.borderColor = variant.current;
    });

    const futureDots = simBox.querySelectorAll('.year-dot.future');
    futureDots.forEach(dot => {
        dot.style.backgroundColor = 'transparent';
        dot.style.borderColor = variant.futureBorder;
    });
}

function initThemeGallery() {
    const galleryContainer = document.getElementById('theme-gallery');
    if (!galleryContainer) return;

    galleryContainer.innerHTML = '';

    Object.values(THEMES).forEach(theme => {
        const card = document.createElement('div');
        card.classList.add('theme-card');
        card.setAttribute('data-theme', theme.id);

        const badgeClass = theme.isRotatable ? 'theme-badge rotatable' : 'theme-badge';
        const badgeText = theme.isRotatable ? 'Rotatable' : theme.category;

        card.innerHTML = `
            <div class="theme-card-header">
                <span class="theme-card-title">${theme.name}</span>
                <span class="${badgeClass}">${badgeText}</span>
            </div>
            <div class="theme-variants">
                <div class="variant-row light-variant">
                    <span class="variant-label"><i class="fa-solid fa-sun"></i> Light</span>
                    <div class="swatch-group">
                        <span class="swatch-dot" style="background: ${theme.light.bg};" title="Background: ${theme.light.bg}"></span>
                        <span class="swatch-dot" style="background: ${theme.light.past};" title="Past: ${theme.light.past}"></span>
                        <span class="swatch-dot" style="background: ${theme.light.current};" title="Current: ${theme.light.current}"></span>
                        <span class="swatch-dot" style="background: ${theme.light.futureBorder};" title="Future Outline"></span>
                    </div>
                </div>
                <div class="variant-row dark-variant">
                    <span class="variant-label"><i class="fa-solid fa-moon"></i> Dark</span>
                    <div class="swatch-group">
                        <span class="swatch-dot" style="background: ${theme.dark.bg};" title="Background: ${theme.dark.bg}"></span>
                        <span class="swatch-dot" style="background: ${theme.dark.past};" title="Past: ${theme.dark.past}"></span>
                        <span class="swatch-dot" style="background: ${theme.dark.current};" title="Current: ${theme.dark.current}"></span>
                        <span class="swatch-dot" style="background: ${theme.dark.futureBorder};" title="Future Outline"></span>
                    </div>
                </div>
            </div>
        `;

        card.addEventListener('click', () => {
            setHeroTheme(theme.id, false);
            const heroSection = document.getElementById('hero');
            if (heroSection) {
                heroSection.scrollIntoView({ behavior: 'smooth' });
            }
        });

        galleryContainer.appendChild(card);
    });
}

function updateTodayRotationBadge() {
    const todayBadgeValue = document.getElementById('today-theme-name');
    if (!todayBadgeValue) return;

    const rotatedThemeId = getRotatedThemeId();
    const rotatedTheme = THEMES[rotatedThemeId];
    if (rotatedTheme) {
        todayBadgeValue.textContent = rotatedTheme.name;
    }
}

function renderMiniMacroGrid() {
    const gridContainer = document.getElementById('mini-macro-grid');
    if (!gridContainer) return;

    gridContainer.innerHTML = '';
    const totalYears = 60;
    const yearsLived = 20;
    const cols = 6;

    gridContainer.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;

    for (let i = 0; i < totalYears; i++) {
        const dot = document.createElement('div');
        dot.classList.add('year-dot');

        if (i < yearsLived) {
            dot.classList.add('past');
        } else if (i === yearsLived) {
            dot.classList.add('current');
        } else {
            dot.classList.add('future');
        }
        gridContainer.appendChild(dot);
    }
}

function renderMicroNoTomorrow() {
    const container = document.getElementById('micro-no-tomorrow');
    if (!container) return;

    // Clean existing except label
    const existing = container.querySelector('.shape-no-tomorrow');
    if (existing) existing.remove();

    const shape = document.createElement('div');
    shape.classList.add('shape-no-tomorrow');
    shape.style.margin = 'auto';

    container.appendChild(shape);
}

function renderMicroVsYesterday() {
    const container = document.getElementById('micro-vs-yesterday');
    if (!container) return;

    const existing = container.querySelector('.vs-container');
    if (existing) existing.remove();

    const vsContainer = document.createElement('div');
    vsContainer.classList.add('vs-container');

    const pastShape = document.createElement('div');
    pastShape.classList.add('shape-past');
    pastShape.title = "Yesterday";

    const presentShape = document.createElement('div');
    presentShape.classList.add('shape-present');
    presentShape.title = "Today";

    vsContainer.appendChild(pastShape);
    vsContainer.appendChild(presentShape);

    container.appendChild(vsContainer);
}

function renderMicroCustom() {
    const gridContainer = document.getElementById('custom-event-grid');
    if (!gridContainer) return;

    gridContainer.innerHTML = '';
    const totalDays = 30;
    const daysPassed = 12;
    const cols = 5;

    gridContainer.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;

    for (let i = 0; i < totalDays; i++) {
        const dot = document.createElement('div');
        dot.classList.add('year-dot');

        if (i < daysPassed) {
            dot.classList.add('past');
        } else if (i === daysPassed) {
            dot.classList.add('current');
            dot.title = "Today";
        } else {
            dot.classList.add('future');
        }
        gridContainer.appendChild(dot);
    }
}

function renderMicroHealth() {
    const gridContainer = document.getElementById('health-event-grid');
    if (!gridContainer) return;

    gridContainer.innerHTML = '';
    const totalDays = 25;
    const daysPassed = 19;
    const cols = 5;

    const data = [
        { val: "10.8k", opacity: 0.9 },
        { val: "16.9k", opacity: 1.0 },
        { val: "13.8k", opacity: 0.95 },
        { val: "14.3k", opacity: 0.98 },
        { val: "2.2k",  opacity: 0.2 },
        { val: "3.4k",  opacity: 0.3 },
        { val: "2.4k",  opacity: 0.25 },
        { val: "4.0k",  opacity: 0.4 },
        { val: "12.8k", opacity: 0.9 },
        { val: "19.8k", opacity: 1.0 },
        { val: "13.2k", opacity: 0.95 },
        { val: "8.3k",  opacity: 0.8 },
        { val: "9.5k",  opacity: 0.95 },
        { val: "8.9k",  opacity: 0.85 },
        { val: "13.2k", opacity: 0.98 },
        { val: "20.5k", opacity: 1.0 },
        { val: "6.1k",  opacity: 0.6 },
        { val: "13.4k", opacity: 0.95 },
        { val: "1.2k",  opacity: 0.1 },
    ];

    gridContainer.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;
    gridContainer.classList.add('rhombus-grid');

    for (let i = 0; i < totalDays; i++) {
        const dot = document.createElement('div');
        dot.classList.add('health-shape');

        if (i < daysPassed) {
            dot.classList.add('past');
            const opacity = data[i] ? data[i].opacity : 0.5;
            dot.style.opacity = opacity;

            const label = document.createElement('span');
            label.textContent = data[i] ? data[i].val : "";
            dot.appendChild(label);
        } else if (i === daysPassed) {
            dot.classList.add('current');
            dot.title = "Today";
            dot.style.opacity = 1.0;

            const label = document.createElement('span');
            label.textContent = "14";
            dot.appendChild(label);
        } else {
            dot.classList.add('future');
        }
        gridContainer.appendChild(dot);
    }
}
