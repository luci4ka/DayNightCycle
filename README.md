# DayNightCycle

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Paper](https://img.shields.io/badge/Paper-26.1-blue)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://adoptium.net/)
[![bStats](https://img.shields.io/badge/bStats-34699-red)](https://bstats.org/plugin/bukkit/DayNightCycle/34699)

Гибкое управление длительностью дня и ночи на Paper-сервере. Плюс встроенная система AFK, пропуска сна и просмотра времени.

---

## ✨ Возможности

- ☀️ **Настройка длительности дня и ночи** — в минутах, отдельно для каждого.
- 🌍 **Выбор миров** — применяется ко всем оверворлдам или только к указанным.
- 😴 **AFK-система** — авто-определение AFK по таймауту, статус в action bar.
- 🔌 **Интеграция с внешними AFK-плагинами** — через PlaceholderAPI (EssentialsX, CMI и др.).
- 🌙 **Пропуск сна** — гибкий процент спящих, AFK-игроки считаются спящими.
- 📋 **Override-список сна** — игроки, которые всегда считаются спящими.
- 🎬 **Анимация ускорения ночи** — плавный переход к рассвету вместо мгновенного.
- ⏰ **Просмотр времени** — `/dnc time` с временем в формате 24 часа.
- 🌐 **Мультиязычность** — 7 языков, YAML-формат, автоматический выбор по локали игрока.
- 📝 **Свои языки** — пользователь может закинуть `fr_fr.yml` в `plugins/DayNightCycle/lang/`.
- 🔌 **PlaceholderAPI** — `%dnc_afk%`, `%dnc_afk_prefix%`, `%dnc_time%`, `%dnc_ticks%`, `%dnc_world%`.
- 📊 **bStats** — анонимная статистика использования.
- 🔔 **Update Checker** — проверка обновлений через GitHub Releases.

---

## 📦 Установка

1. Скачай `DayNightCycle-1.0.jar`.
2. Положи в папку `plugins/` на сервере.
3. Установи зависимости:
   - [CommandAPI](https://modrinth.com/plugin/commandapi) — **обязательно**.
   - [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) — опционально (для плейсхолдеров и интеграции с внешним AFK).
4. Перезапусти сервер.
5. Настрой `plugins/DayNightCycle/config.yml`.

**Требования:** Paper 26.1+, Java 25. На чистом Spigot/Bukkit не запустится.

---

## ⚙️ Конфигурация

### `config.yml`

```yaml
# Язык по умолчанию (ru_ru, en_us, de_de, zh_cn, es_es, kk_kz, be_by)
# Свой язык: положите <code>.yml в plugins/DayNightCycle/lang/ и укажите код здесь.
language: ru_ru

# Проверять обновления при старте (GitHub Releases)
update-check: true

cycle:
  enabled: true
  # Длительность дня и ночи в минутах
  day-minutes: 20
  night-minutes: 10
  # Миров, к которым применяется (пусто = все оверворлды)
  worlds: []
  # Отключить ванильный цикл (ADVANCE_TIME).
  # Если false — ваш цикл и ванильный работают одновременно, время идёт вдвое быстрее.
  disable-vanilla-cycle: true

sleep:
  enabled: true
  # Процент спящих от онлайн-игроков в мире (0.0–1.0)
  percent: 0.5
  # AFK-игроки считаются спящими
  afk-counts-as-sleeping: true
  # Сообщение о пропуске ночи
  broadcast-skip: true
  # Сбрасывать погоду при пропуске ночи
  clear-weather: true
  animation:
    enabled: true
    duration-seconds: 3

afk:
  enabled: true
  timeout-minutes: 5
  reset-on-move: true
  reset-on-chat: true
  reset-on-interact: true
  smart-movement: true
  display:
    text: "AFK"
  # Если на сервере уже есть другой AFK-плагин, можно читать статус оттуда
  # через PlaceholderAPI вместо встроенной системы.
  external:
    enabled: false
    # Любой плейсхолдер, возвращающий "true"/"yes"/"1" для AFK-игрока.
    # Примеры:
    #   %essentials_afk%          — EssentialsX
    #   %cmi_user_afk%            — CMI
    #   %afk%                     — лёгкие AFK-плагины
    placeholder: "%essentials_afk%"
```

### `sleeplist.yml`

```yaml
# Игроки, которые всегда считаются спящими (case-insensitive)
players:
  - Ivan
```

---

## 🎮 Команды

| Команда | Описание | Право |
|---|---|---|
| `/dnc` | Показать информацию о плагине | `dnc.command.status` |
| `/dnc info` | Информация, версия, языки, статус интеграций | `dnc.command.info` |
| `/dnc status` | Текущие настройки: день, ночь, AFK | `dnc.command.status` |
| `/dnc time` | Показать игровое время в текущем мире | `dnc.command.time` |
| `/dnc reload` | Перезагрузить конфигурацию | `dnc.command.reload` |
| `/dnc set day <мин>` | Установить длительность дня | `dnc.command.set` |
| `/dnc set night <мин>` | Установить длительность ночи | `dnc.command.set` |
| `/dnc sleeplist` | Показать override-список | `dnc.command.sleeplist.view` |
| `/dnc sleeplist add <ник>` | Добавить игрока в override | `dnc.command.sleeplist` |
| `/dnc sleeplist remove <ник>` | Удалить игрока из override | `dnc.command.sleeplist` |
| `/afk` | Переключить AFK-статус | `dnc.afk` |

### ⏰ `/dnc time` — пример вывода

```
▬▬▬▬▬ Время в мире ▬▬▬▬▬
Мир: Выживание
Время: 14:30 (8500 тиков)
▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬
```

**Цвета миров:**
- `Выживание` — зелёный
- `Ад` — оранжевый (ошибки/опасность)
- `Край` — фиолетовый
- Кастомные миры — серый (с именем мира)

---

## 🔑 Права

| Право | Описание | По умолчанию |
|---|---|---|
| `dnc.command.status` | Доступ к `/dnc` и `/dnc status` | Все |
| `dnc.command.info` | Доступ к `/dnc info` | Все |
| `dnc.command.time` | Доступ к `/dnc time` | Все |
| `dnc.command.reload` | Доступ к `/dnc reload` | OP |
| `dnc.command.set` | Доступ к `/dnc set` | OP |
| `dnc.command.sleeplist` | Управление override-списком | OP |
| `dnc.command.sleeplist.view` | Просмотр override-списка | OP (дочернее у `.sleeplist`) |
| `dnc.afk` | Доступ к `/afk` | Все |
| `dnc.bypass` | Не становится AFK, не считается в сне | OP |

---

## 🔌 PlaceholderAPI

| Плейсхолдер | Описание |
|---|---|
| `%dnc_afk%` | Текст AFK, если игрок в AFK, иначе пусто |
| `%dnc_afk_prefix%` | То же + пробел |
| `%dnc_time%` | Игровое время в формате `HH:mm` |
| `%dnc_ticks%` | Игровое время в тиках |
| `%dnc_world%` | Название мира (цветное) |

**Пример использования в TAB:**
```yaml
scoreboards:
  - name: "info"
    lines:
      - "&7Время: &f%dnc_time%"
      - "&7Мир: &f%dnc_world%"
      - "&7AFK: &f%dnc_afk_prefix%%player%"
```

---

## 🔗 Интеграция с внешними AFK-плагинами

Если на сервере уже работает EssentialsX, CMI или другой AFK-плагин, можно использовать **его** статус вместо встроенной системы. Это делается через PlaceholderAPI — без жёсткой зависимости от конкретного плагина.

В `config.yml`:

```yaml
afk:
  external:
    enabled: true
    placeholder: "%essentials_afk%"
```

Плагин будет считать игрока AFK, если указанный плейсхолдер вернёт `true`, `yes` или `1`. Подходят, например:

| Плагин AFK | Плейсхолдер |
|---|---|
| EssentialsX | `%essentials_afk%` |
| CMI | `%cmi_user_afk%` |
| лёгкие AFK-плагины | `%afk%` |

**Требования:** установленный PlaceholderAPI и соответствующий плагин-поставщик плейсхолдера.

Если `external.enabled: false` (по умолчанию) — используется встроенная AFK-система.

---

## 🌐 Языки

Плагин поддерживает локализацию. Файлы лежат в `plugins/DayNightCycle/lang/` в формате **YAML**:

- `ru_ru.yml` — русский
- `en_us.yml` — английский
- `de_de.yml` — немецкий
- `zh_cn.yml` — упрощённый китайский

**Добавить новый язык:**

1. Скопируй `plugins/DayNightCycle/lang/en_us.yml` → `fr_fr.yml`.
2. Переведи значения (все строки — в кавычках).
3. Выполни `/dnc reload` (или перезапусти сервер).

Плагин автоматически подхватит язык игрока через `player.locale()`. Если языка нет — fallback на `ru_ru`.

**Особенности загрузки:**

- Имя файла нормализуется: `fr.yml`, `fr-FR.yml`, `FR_fr.yml` → `fr_fr`.
- Если файл неполный — отсутствующие ключи подтягиваются из `ru_ru`, в логе будет предупреждение с числом недостающих ключей.
- Если файл битый (невалидный YAML) — в логе будет `Invalid YAML in fr_fr.yml (...)`, остальные языки загрузятся.
- Если файл пустой — он будет пропущен с предупреждением.

---

## ❓ FAQ

### 🔹 AFK-статус не снимается при движении

**Причина:** включён `smart-movement: true`, который игнорирует микро-сдвиги.

**Решение:**
- Если хочешь, чтобы **любое** движение снимало AFK — поставь `smart-movement: false`.
- Если оставляешь `true` — игрок должен сдвинуться на блок по X/Z или на 0.5 по Y.

---

### 🔹 Игрок в AFK не считается спящим

**Причина:** в `config.yml` отключено `afk-counts-as-sleeping`.

**Решение:**
```yaml
sleep:
  afk-counts-as-sleeping: true
```

---

### 🔹 Ночь не пропускается, хотя все спят

**Причина:** не хватает `percent` от онлайн-игроков.

**Решение:**
- Проверь `/dnc status` — сколько AFK и сколько требуется.
- Уменьши `sleep.percent` в `config.yml` (например, `0.3` = 30%).
- Добавь игроков в `sleeplist.yml`, если они должны считаться спящими всегда.

---

### 🔹 Как добавить свой язык?

1. Скопируй `plugins/DayNightCycle/lang/en_us.yml` → `fr_fr.yml`.
2. Переведи значения. **Все строки должны быть в кавычках**, если содержат `<`, `%`, `:` и т.п.
3. Выполни `/dnc reload` (или перезапусти сервер).

**Важно:** в YAML не должно быть дубликатов ключей — последнее объявление побеждает.

---

### 🔹 Как изменить длительность дня и ночи?

**Через команду:**
```
/dnc set day 30
/dnc set night 15
```

**Через конфиг:** отредактируй `plugins/DayNightCycle/config.yml` и выполни `/dnc reload`.

**Единицы:** минуты. `day-minutes: 20` = день длится 20 реальных минут.

---

### 🔹 У меня стоит EssentialsX/CMI с их AFK. Можно использовать?

Да. Включи в `config.yml`:

```yaml
afk:
  external:
    enabled: true
    placeholder: "%essentials_afk%"   # или "%cmi_user_afk%"
```

Требуется PlaceholderAPI и соответствующий плагин-поставщик плейсхолдера.

---

### 🔹 Работает ли плагин на Spigot / Bukkit?

**Нет.** Плагин использует Paper API (Adventure, CommandAPI, GameRules). На чистом Spigot не запустится.

**Минимум:** Paper 26.1. Работает на форках: Purpur, Pufferfish, Leaf.

---

### 🔹 Как интегрировать с TAB?

Используй плейсхолдеры в конфиге TAB:

```yaml
scoreboards:
  - name: "info"
    lines:
      - "&7Время: &f%dnc_time%"
      - "&7AFK: &f%dnc_afk_prefix%%player%"
```

---

### 🔹 Куда сообщать о багах?

- [GitHub Issues](https://github.com/luci4ka/DayNightCycle/issues)

Приложи:
1. Версию плагина (`/version DayNightCycle`).
2. Версию Paper (`/version`).
3. Лог с ошибкой (`logs/latest.log`).
4. Шаги для воспроизведения.

---

## 🔧 Сборка из исходников

### Требования

- Java 25
- Gradle 9.x

### Сборка

```bash
git clone https://github.com/luci4ka/DayNightCycle.git
cd DayNightCycle
./gradlew clean shadowJar
```

Готовый jar: `build/libs/DayNightCycle-1.0.jar`

---

## 🛠️ Технологии

- **Paper API 26.1**
- **CommandAPI 12.1.0** — регистрация команд
- **ConfigLib 4.8.1** — YAML-конфиги с автогенерацией
- **Adventure / MiniMessage** — форматирование текста
- **bStats 3.2.1** — метрики
- **mc-plugin-update-notifier** — проверка обновлений

---

## 📋 Структура проекта

```
DayNightCycle/
├── src/main/java/ru/terracraft/daynightcycle/
│   ├── DayNightCyclePlugin.java     # главный класс
│   ├── afk/                         # AFK-система (+ внешняя через PAPI)
│   ├── command/                     # команды
│   ├── config/                      # конфиги
│   ├── cycle/                       # управление временем
│   ├── lang/                        # i18n (YAML)
│   ├── listener/                    # WorldListener
│   ├── placeholder/                 # PlaceholderAPI
│   ├── sleep/                       # пропуск сна
│   └── update/                      # проверка обновлений
├── src/main/resources/
│   ├── paper-plugin.yml
│   └── lang/
│       ├── ru_ru.yml
│       ├── en_us.yml
│       ├── zh_cn.yml
│       ├── es_es.yml
├── build.gradle
├── LICENSE
└── README.md
```

---

## 📄 Лицензия

Этот проект распространяется под лицензией **MIT**. См. [LICENSE](LICENSE).

---

## 🐛 Баги и предложения

Если нашёл баг или есть идея — создай issue в [GitHub Issues](https://github.com/luci4ka/DayNightCycle/issues).