package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.concurrent.CompletableFuture;
import java.util.*;


public class TarotBot extends TelegramLongPollingBot{

    private static final String BOT_USERNAME = "BezdarlarBot";
    private static final String BOT_TOKEN = "7932957123:AAEzBkv3n3EwNZ-IP3cfsvvGs_DUqawpcW4";

    private static final Random random = new Random();

    private String selectedTarotType = "";
    private String cardList = "";
    private Map<Long, UserLayoutState> userLayouts = new HashMap<>();
    
    private static final Map<String, String> tarotDeck = new HashMap<>();
    static {
        tarotDeck.put("Шут", "Новые начинания, свобода, спонтанность");
        tarotDeck.put("Шут (перевернуто)", "Безрассудство, неготовность, неопределенность");

        tarotDeck.put("Маг", "Сила воли, мастерство, изобретательность");
        tarotDeck.put("Маг (перевернуто)", "Манипуляции, обман, утрата контроля");

        tarotDeck.put("Верховная Жрица", "Интуиция, тайные знания, мудрость");
        tarotDeck.put("Верховная Жрица (перевернуто)", "Неопределенность, скрытые истины, игнорирование интуиции");

        tarotDeck.put("Императрица", "Материнство, плодородие, природа");
        tarotDeck.put("Императрица (перевернуто)", "Творческий застой, неуверенность, недостаток заботы");

        tarotDeck.put("Император", "Стабильность, власть, структура");
        tarotDeck.put("Император (перевернуто)", "Тирания, злоупотребление властью, жесткость");

        tarotDeck.put("Иерофант", "Традиции, духовное руководство, обучение");
        tarotDeck.put("Иерофант (перевернуто)", "Отклонение от традиций, ограниченные взгляды, противоречие");

        tarotDeck.put("Влюбленные", "Любовь, гармония, выбор, партнерство");
        tarotDeck.put("Влюбленные (перевернуто)", "Неопределенность, разрыв, неправильный выбор");

        tarotDeck.put("Колесница", "Контроль, движение вперед, победа");
        tarotDeck.put("Колесница (перевернуто)", "Потеря контроля, неудача, стагнация");

        tarotDeck.put("Сила", "Мужество, стойкость, внутренняя сила");
        tarotDeck.put("Сила (перевернуто)", "Слабость, отсутствие уверенности, излишняя агрессия");

        tarotDeck.put("Отшельник", "Поиск, одиночество, мудрость");
        tarotDeck.put("Отшельник (перевернуто)", "Изоляция, депрессия, отказ от поиска");

        tarotDeck.put("Колесо Фортуны", "Удача, перемены, судьба");
        tarotDeck.put("Колесо Фортуны (перевернуто)", "Неудача, застой, сопротивление переменам");

        tarotDeck.put("Правосудие", "Справедливость, баланс, честность");
        tarotDeck.put("Правосудие (перевернуто)", "Несправедливость, предвзятость, нарушение закона");

        tarotDeck.put("Повешенный", "Жертвенность, пауза, новое восприятие");
        tarotDeck.put("Повешенный (перевернуто)", "Упорство, неготовность меняться, стагнация");

        tarotDeck.put("Смерть", "Конец, трансформация, новое начало");
        tarotDeck.put("Смерть (перевернуто)", "Страх перемен, задержка, сопротивление завершению");

        tarotDeck.put("Умеренность", "Баланс, гармония, терпение, самоконтроль");
        tarotDeck.put("Умеренность (перевернуто)", "Избыточность, крайности, потеря контроля");

        tarotDeck.put("Дьявол", "Привязанности, соблазны, искушения");
        tarotDeck.put("Дьявол (перевернуто)", "Освобождение, преодоление зависимостей, разрушение иллюзий");

        tarotDeck.put("Башня", "Кризис, разрушение, внезапные перемены");
        tarotDeck.put("Башня (перевернуто)", "Избежание разрушения, страх перемен, отказ от изменений");

        tarotDeck.put("Звезда", "Надежда, вдохновение, вера");
        tarotDeck.put("Звезда (перевернуто)", "Пессимизм, разочарование, утрата веры");

        tarotDeck.put("Луна", "Секреты, иллюзии, страхи");
        tarotDeck.put("Луна (перевернуто)", "Чистота, раскрытие тайн, преодоление страха");

        tarotDeck.put("Солнце", "Радость, успех, счастье");
        tarotDeck.put("Солнце (перевернуто)", "Печаль, неудача, утрата ясности");

        tarotDeck.put("Суд", "Возрождение, осознание, кармическое завершение");
        tarotDeck.put("Суд (перевернуто)", "Невозможность двигаться вперед, сожаления, нерешительность");

        tarotDeck.put("Мир", "Завершение, достижение, гармония");
        tarotDeck.put("Мир (перевернуто)", "Неоконченные дела, разочарование, неполный успех");

        tarotDeck.put("Желание", "Стремление, мотивация, амбиции, сильное желание чего-то");
        tarotDeck.put("Желание (перевернуто)", "Неудовлетворенность, беспокойство, невыполненные амбиции");

        tarotDeck.put("Мечта", "Вдохновение, амбиции, мечты, стремление к идеалам");
        tarotDeck.put("Мечта (перевернуто)", "Иллюзии, потерянные надежды, непрактичность, расставание с мечтами");

        tarotDeck.put("Отчаяние", "Печаль, утрата надежды, безысходность, кризис");
        tarotDeck.put("Отчаяние (перевернуто)", "Выход из кризиса, восстановление, преодоление трудностей");

        tarotDeck.put("Разрушение", "Конец, кризис, катастрофа, разрушение старых структур");
        tarotDeck.put("Разрушение (перевернуто)", "Преодоление разрушений, избегание катастрофы, восстановление");

        tarotDeck.put("Судьба", "Предназначение, кармические уроки, неизбежные события");
        tarotDeck.put("Судьба (перевернуто)", "Сопротивление судьбе, отложенные кармические уроки, замедленные перемены");

        tarotDeck.put("Счастье", "Радость, удовлетворение, гармония, исполнение желаний");
        tarotDeck.put("Счастье (перевернуто)", "Неудовлетворенность, утрата радости, пустота, внутреннее беспокойство");

        tarotDeck.put("Безумие", "Неуправляемые эмоции, освобождение от норм, хаос, иррациональность");
        tarotDeck.put("Безумие (перевернуто)", "Восстановление контроля, внутренний порядок, возвращение к разуму");

        tarotDeck.put("Король Жезлов", "Лидерство, страсть, уверенность, инициатива");
        tarotDeck.put("Король Жезлов (перевернуто)", "Тирания, недостаток контроля, слишком большая агрессия");

        tarotDeck.put("Королева Жезлов", "Тепло, забота, интуиция, уверенность, вдохновение");
        tarotDeck.put("Королева Жезлов (перевернуто)", "Излишняя доминирующая роль, эмоциональная нестабильность, чрезмерная страсть");

        tarotDeck.put("Рыцарь Жезлов", "Действие, энергия, амбиции, стремление к новым приключениям");
        tarotDeck.put("Рыцарь Жезлов (перевернуто)", "Безрассудство, импульсивность, неумение завершать начатое");

        tarotDeck.put("Паж Жезлов", "Новое начало, вдохновение, исследование, любопытство");
        tarotDeck.put("Паж Жезлов (перевернуто)", "Неопытность, неуверенность, отсутствие фокуса");

        tarotDeck.put("Десятка Жезлов", "Бремя ответственности, трудности, нагрузка");
        tarotDeck.put("Десятка Жезлов (перевернуто)", "Освобождение от тяжести, преодоление препятствий, облегчение");

        tarotDeck.put("Девятка Жезлов", "Стойкость, защита, готовность к трудностям");
        tarotDeck.put("Девятка Жезлов (перевернуто)", "Паралич, усталость, недостаток решимости");

        tarotDeck.put("Восьмерка Жезлов", "Быстрое движение, действия, прогресс, новости");
        tarotDeck.put("Восьмерка Жезлов (перевернуто)", "Задержки, замедление, препятствия на пути");

        tarotDeck.put("Семерка Жезлов", "Защита своей позиции, стойкость, уверенность в себе");
        tarotDeck.put("Семерка Жезлов (перевернуто)", "Неуверенность, капитуляция, чувство уязвимости");

        tarotDeck.put("Шестерка Жезлов", "Победа, признание, успех, триумф");
        tarotDeck.put("Шестерка Жезлов (перевернуто)", "Неудача, недостаток признания, падение с пьедестала");

        tarotDeck.put("Пятерка Жезлов", "Конкуренция, борьба, конфликт");
        tarotDeck.put("Пятерка Жезлов (перевернуто)", "Избежание конфликта, уступчивость, преодоление разногласий");

        tarotDeck.put("Четверка Жезлов", "Стабильность, празднование, гармония, успех");
        tarotDeck.put("Четверка Жезлов (перевернуто)", "Неудача в достижении целей, проблемы в отношениях, нестабильность");

        tarotDeck.put("Тройка Жезлов", "Ожидания, перспектива, расширение, начало нового этапа");
        tarotDeck.put("Тройка Жезлов (перевернуто)", "Неудачные планы, блокировка прогресса, ограниченные возможности");

        tarotDeck.put("Двойка Жезлов", "Планирование, выбор, принятие решений, первые шаги к успеху");
        tarotDeck.put("Двойка Жезлов (перевернуто)", "Откладывание решений, неопределенность, страх изменений");

        tarotDeck.put("Туз Жезлов", "Новые начинания, вдохновение, энергия, потенциал");
        tarotDeck.put("Туз Жезлов (перевернуто)", "Недостаток энергии, упущенные возможности, отсутствие инициативы");

        tarotDeck.put("Король Кубков", "Эмоциональный баланс, сочувствие, зрелость, контроль над чувствами");
        tarotDeck.put("Король Кубков (перевернуто)", "Эмоциональная нестабильность, манипуляции, отсутствие контроля");

        tarotDeck.put("Королева Кубков", "Чувствительность, интуиция, забота, эмоциональная зрелость");
        tarotDeck.put("Королева Кубков (перевернуто)", "Эмоциональная зависимость, манипуляции, излишняя чувствительность");

        tarotDeck.put("Рыцарь Кубков", "Романтика, идеализм, стремление к новым эмоциональным переживаниям");
        tarotDeck.put("Рыцарь Кубков (перевернуто)", "Нереалистичные ожидания, сентиментальность, манипуляции через эмоции");

        tarotDeck.put("Паж Кубков", "Вдохновение, новые эмоции, творческое начало, возможность нового любовного опыта");
        tarotDeck.put("Паж Кубков (перевернуто)", "Неопытность, эмоциональная закрытость, разочарования");

        tarotDeck.put("Десятка Кубков", "Семейное счастье, гармония, эмоциональное удовлетворение");
        tarotDeck.put("Десятка Кубков (перевернуто)", "Неудовлетворенность в отношениях, разочарование в семье, кризис");

        tarotDeck.put("Девятка Кубков", "Исполнение желаний, счастье, удовлетворение, эмоциональный успех");
        tarotDeck.put("Девятка Кубков (перевернуто)", "Нереалистичные ожидания, пустота, неудачи");

        tarotDeck.put("Восьмерка Кубков", "Покидание старых эмоций, переход к новым целям, внутренняя трансформация");
        tarotDeck.put("Восьмерка Кубков (перевернуто)", "Привязанность, неготовность оставить прошлое, замедление изменений");

        tarotDeck.put("Семерка Кубков", "Мечты, иллюзии, выбор, множество возможностей");
        tarotDeck.put("Семерка Кубков (перевернуто)", "Ясность, отказ от иллюзий, реальное восприятие");

        tarotDeck.put("Шестерка Кубков", "Ностальгия, воспоминания, возвращение к прошлому, чистота эмоций");
        tarotDeck.put("Шестерка Кубков (перевернуто)", "Застой, переживания по поводу прошлого, избегание изменений");

        tarotDeck.put("Пятерка Кубков", "Печаль, утрата, сожаление, сосредоточенность на негативе");
        tarotDeck.put("Пятерка Кубков (перевернуто)", "Принятие потерь, восстановление, выход из депрессии");

        tarotDeck.put("Четверка Кубков", "Отчужденность, скука, самозамкнутость, эмоциональная инертность");
        tarotDeck.put("Четверка Кубков (перевернуто)", "Отворение от себя, новые возможности, готовность к изменениям");

        tarotDeck.put("Тройка Кубков", "Праздник, радость, поддержка со стороны друзей, гармония в отношениях");
        tarotDeck.put("Тройка Кубков (перевернуто)", "Конфликты в отношениях, социальная изоляция, расставание");

        tarotDeck.put("Двойка Кубков", "Любовь, партнерство, гармония в отношениях, союз душ");
        tarotDeck.put("Двойка Кубков (перевернуто)", "Разрыв, негармоничные отношения, неверность");

        tarotDeck.put("Туз Кубков", "Новые эмоциональные начала, любовь, вдохновение, раскрытие сердца");
        tarotDeck.put("Туз Кубков (перевернуто)", "Эмоциональная блокировка, неготовность принять любовь, потерянные возможности");

        tarotDeck.put("Король Мечей", "Разум, объективность, логика, принятие решений, лидерство");
        tarotDeck.put("Король Мечей (перевернуто)", "Жестокость, манипуляции, злоупотребление властью, чрезмерная строгость");

        tarotDeck.put("Королева Мечей", "Интеллект, независимость, мудрость, проницательность");
        tarotDeck.put("Королева Мечей (перевернуто)", "Холодность, изоляция, беспокойство, недоверие");

        tarotDeck.put("Рыцарь Мечей", "Решительность, стремление к цели, активность, целеустремленность");
        tarotDeck.put("Рыцарь Мечей (перевернуто)", "Безрассудство, агрессия, поспешность, неосторожность");

        tarotDeck.put("Паж Мечей", "Наблюдательность, любопытство, новые идеи, учеба");
        tarotDeck.put("Паж Мечей (перевернуто)", "Недостаток информации, ложные обвинения, недооценка");

        tarotDeck.put("Десятка Мечей", "Конец, поражение, окончание трудного периода, предательство");
        tarotDeck.put("Десятка Мечей (перевернуто)", "Восстановление, новый старт, конец страданий, облегчение");

        tarotDeck.put("Девятка Мечей", "Беспокойство, ночные кошмары, страхи, сожаление");
        tarotDeck.put("Девятка Мечей (перевернуто)", "Освобождение от страха, выход из депрессии, ясность мыслей");

        tarotDeck.put("Восьмерка Мечей", "Ограничения, ловушка, чувство безысходности, препятствия");
        tarotDeck.put("Восьмерка Мечей (перевернуто)", "Выход из ограничений, освобождение, преодоление страхов");

        tarotDeck.put("Семерка Мечей", "Обман, хитрость, скрытые намерения, ложь");
        tarotDeck.put("Семерка Мечей (перевернуто)", "Раскрытие обмана, правда, разоблачение, отказ от лжи");

        tarotDeck.put("Шестерка Мечей", "Путешествие, переход, освобождение, улучшение ситуации");
        tarotDeck.put("Шестерка Мечей (перевернуто)", "Задержки, невозможность двигаться дальше, застой");

        tarotDeck.put("Пятерка Мечей", "Конфликт, борьба, победа через обман, разногласия");
        tarotDeck.put("Пятерка Мечей (перевернуто)", "Разрешение конфликта, отказ от борьбы, признание ошибки");

        tarotDeck.put("Четверка Мечей", "Отдых, восстановление, обдумывание, передышка");
        tarotDeck.put("Четверка Мечей (перевернуто)", "Беспокойство, необходимость покоя, нервозность, стресс");

        tarotDeck.put("Тройка Мечей", "Разрыв, печаль, сердце, разочарования");
        tarotDeck.put("Тройка Мечей (перевернуто)", "Исцеление от боли, восстановление, освобождение от страха");

        tarotDeck.put("Двойка Мечей", "Неопределенность, принятие решений, внутренний конфликт");
        tarotDeck.put("Двойка Мечей (перевернуто)", "Ясность, принятие решения, окончательный выбор, разрыв");

        tarotDeck.put("Туз Мечей", "Ясность, новая идея, откровение, победа разума");
        tarotDeck.put("Туз Мечей (перевернуто)", "Иллюзии, путаница, ложные идеи, отсутствие четкости");

        tarotDeck.put("Король Пентаклей", "Материальное благополучие, стабильность, ответственность, успех в делах");
        tarotDeck.put("Король Пентаклей (перевернуто)", "Жадность, чрезмерная привязанность к деньгам, излишняя консервативность");

        tarotDeck.put("Королева Пентаклей", "Уход, забота, практичность, хозяйственность, умение обеспечивать");
        tarotDeck.put("Королева Пентаклей (перевернуто)", "Чрезмерная привязанность к материальным вещам, скука, недостаток заботы");

        tarotDeck.put("Рыцарь Пентаклей", "Усердие, терпение, методичность, ответственность");
        tarotDeck.put("Рыцарь Пентаклей (перевернуто)", "Медлительность, застой, отсутствие прогресса, перегрузка обязанностями");

        tarotDeck.put("Паж Пентаклей", "Новые начинания, стремление к обучению, возможности для материального роста");
        tarotDeck.put("Паж Пентаклей (перевернуто)", "Неопытность, недооценка возможностей, медленный старт");

        tarotDeck.put("Десятка Пентаклей", "Материальное благополучие, семья, традиции, долгосрочные достижения");
        tarotDeck.put("Десятка Пентаклей (перевернуто)", "Финансовые проблемы, потеря стабильности, семейные трудности");

        tarotDeck.put("Девятка Пентаклей", "Самодостаточность, финансовая независимость, наслаждение плодами своего труда");
        tarotDeck.put("Девятка Пентаклей (перевернуто)", "Неудовлетворенность, проблемы с деньгами, чувство одиночества");

        tarotDeck.put("Восьмерка Пентаклей", "Труд, мастерство, усердие, обучение новому, совершенствование навыков");
        tarotDeck.put("Восьмерка Пентаклей (перевернуто)", "Недостаток усилий, халатность, невыполнение обещаний, стагнация");

        tarotDeck.put("Семерка Пентаклей", "Ожидание результатов, анализ труда, терпение, размышления о будущем");
        tarotDeck.put("Семерка Пентаклей (перевернуто)", "Неудачные ожидания, неуспешное планирование, усталость от труда");

        tarotDeck.put("Шестерка Пентаклей", "Щедрость, благотворительность, баланс между даванием и получением");
        tarotDeck.put("Шестерка Пентаклей (перевернуто)", "Неравенство, несправедливость, манипуляции с деньгами");

        tarotDeck.put("Пятерка Пентаклей", "Бедность, трудности, материальные потери, чувство одиночества");
        tarotDeck.put("Пятерка Пентаклей (перевернуто)", "Преодоление бедности, улучшение материального положения, помощь извне");

        tarotDeck.put("Четверка Пентаклей", "Стабильность, накопления, удержание ресурсов, защита материальных ценностей");
        tarotDeck.put("Четверка Пентаклей (перевернуто)", "Жадность, страх потерь, излишняя привязанность к материальным вещам");

        tarotDeck.put("Тройка Пентаклей", "Командная работа, мастерство, признание усилий, сотрудничество");
        tarotDeck.put("Тройка Пентаклей (перевернуто)", "Неудачное сотрудничество, некачественная работа, недостаток признания");

        tarotDeck.put("Двойка Пентаклей", "Балансировка, управление несколькими задачами, гибкость");
        tarotDeck.put("Двойка Пентаклей (перевернуто)", "Перегрузка, потеря баланса, нестабильность");

        tarotDeck.put("Туз Пентаклей", "Новые материальные возможности, начало прибыльного проекта, успех в бизнесе");
        tarotDeck.put("Туз Пентаклей (перевернуто)", "Неоправданные ожидания, упущенные возможности, неудачные начинания");

    }

    private static final Map<String, String> tarotLayouts = new LinkedHashMap<>();
    static {
        tarotLayouts.put("one_card", "🃏 Однокарточный");
        tarotLayouts.put("three_pnb", "🔮 Трёхкарточный");
        tarotLayouts.put("karmic", "🔁 Кармический путь – предназначение");
        tarotLayouts.put("choice", "⚖️ Расклад 'Выбор'");
        tarotLayouts.put("horseshoe", "🔹 Подкова");
        tarotLayouts.put("diagnostic", "🛠️ Диагностика – причины и решения");
        tarotLayouts.put("celtic_cross", "✨ Кельтский крест");
        tarotLayouts.put("yearly", "📅 Годовой прогноз");
    }

    private static final Map<String, Integer> tarotLayoutCardCounts = new HashMap<>();
    static {
        tarotLayoutCardCounts.put("one_card", 1);
        tarotLayoutCardCounts.put("three_pnb", 3);
        tarotLayoutCardCounts.put("karmic", 5);
        tarotLayoutCardCounts.put("choice", 6);
        tarotLayoutCardCounts.put("horseshoe", 7);
        tarotLayoutCardCounts.put("diagnostic", 8);
        tarotLayoutCardCounts.put("celtic_cross", 10);
        tarotLayoutCardCounts.put("yearly", 12);
    }


    public String drawCard() {
        List<String> keys = new ArrayList<>(tarotDeck.keySet());
        String card = keys.get(random.nextInt(keys.size()));
        cardList = "Ваша карта: *" + card + "*\n📖 Значение: _" + tarotDeck.get(card) + "_";
        return cardList;
    }

    public String drawCards(String layout, int amount) {
        List<String> drawnCards = new ArrayList<>();
        StringBuilder result = new StringBuilder(tarotLayouts.get(layout) + "\n\n");

        for (int i = 0; i < amount; i++) {
            String cardDraw;

            do {
                cardDraw = drawCard();
            } while (drawnCards.contains(cardDraw));

            drawnCards.add(cardDraw);
            result.append((i + 1)).append(" : ").append(cardDraw).append("\n");
        }
        cardList = String.join(", ", drawnCards);
        return result.toString();
    }


    public void handleTarotAnalysis(long chatId) {
        sendTextMessage(chatId, "🔮 Анализирую расклад, подождите...");

        CompletableFuture.supplyAsync(() -> {
            DeepSeekChat ai = new DeepSeekChat();
            return ai.generateTarotReading(cardList, selectedTarotType.isEmpty() ? "Общий расклад" : selectedTarotType);
        }).thenAccept(response -> sendTextMessage(chatId, response));
    }


    @Override
    public void onUpdateReceived(Update update) {
        long chatId;
        if (update.hasMessage() && update.getMessage().hasText()) {
            chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText().toLowerCase();

            switch (messageText) {
                case "/start":
                    sendMainMenu(chatId);
                    break;
                case "🔮 вытянуть карту":
                    userLayouts.clear();
                    sendCardButton(chatId, drawCard());
                    break;
                case "🃏 сделать расклад":
                    sendLayoutMenu(chatId, 0);
                    break;
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.startsWith("page_")) {
                editLayoutMenu(chatId, messageId, Integer.parseInt(callbackData.split("_")[1]));
            } else if (callbackData.equals("draw_card")) {
                sendCardButton(chatId, drawCard());
            } else if (callbackData.equals("🃏 Сделать расклад")) {
                sendLayoutMenu(chatId, 0);
            } else if (callbackData.startsWith("type_")) {
                selectedTarotType = callbackData.substring(5);
                editLayoutMenu(chatId, messageId, 1);
            } else if (callbackData.startsWith("layout_")) {
                processLayoutSelection(chatId, messageId, callbackData.substring(7));
            } else if (callbackData.equals("🔄 Заново")) {
                processRedraw(chatId, messageId);
            } else if (callbackData.equals("🔮 Проанализировать расклад")) {
                handleTarotAnalysis(chatId);
            }
        }
    }


    private void processLayoutSelection(long chatId, int messageId, String layoutKey) {
        if (tarotLayouts.containsKey(layoutKey)) {
            int amount = tarotLayoutCardCounts.getOrDefault(layoutKey, 1);
            String text = drawCards(layoutKey, amount);
            editCardButton(chatId, messageId, text);
            userLayouts.put(chatId, new UserLayoutState(layoutKey, amount));
        } else {
            editCardButton(chatId, messageId, "Ошибка: неверный расклад.");
        }
    }


    private void processRedraw(long chatId, int messageId) {
        UserLayoutState userState = userLayouts.get(chatId);
        if (userState != null) {
            editCardButton(chatId, messageId, drawCards(userState.getLayoutKey(), userState.getAmount()));
        } else {
            editCardButton(chatId, messageId, drawCard());
        }
    }


    private static class UserLayoutState {
        private String layoutKey;
        private int amount;

        public UserLayoutState(String layoutKey, int amount) {
            this.layoutKey = layoutKey;
            this.amount = amount;
        }

        public String getLayoutKey() {
            return layoutKey;
        }

        public int getAmount() {
            return amount;
        }
    }


    private static final Map<String, String> tarotTypes = new LinkedHashMap<>();
    static {
        tarotTypes.put("decision", "🔮 Принятие решения");
        tarotTypes.put("love", "🔮 Личная жизнь");
        tarotTypes.put("future", "🔮 Будущее");
        tarotTypes.put("relationship", "🔮 Отношения");
        tarotTypes.put("friendship", "🔮 Дружба");
        tarotTypes.put("personality", "🔮 Личность");
    }


    public void sendMainMenu(long chatId) {
        Logger log = LoggerFactory.getLogger(getClass());

        SendMessage message = new SendMessage();
        message.setText("Выберите действие:");
        message.setChatId(chatId);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow row1 = new KeyboardRow(List.of(
                new KeyboardButton("🔮 Вытянуть карту"),
                new KeyboardButton("🃏 Сделать расклад")
        ));

        KeyboardRow row2 = new KeyboardRow(List.of(
                new KeyboardButton("Опоздаю")
        ));

        keyboardMarkup.setKeyboard(List.of(row1, row2));
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке главного меню", e);
        }
    }


    public void sendLayoutMenu(long chatId, int page) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(getMenuText(chatId, page));
        message.setReplyMarkup(getMenuMarkup(page));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void editLayoutMenu(long chatId, int messageId, int page) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(getMenuText(chatId,page));
        message.setReplyMarkup(getMenuMarkup(page));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String getMenuText(long chatId, int page) {
        if (page == 0) {
            return "Выберите тип расклада:";
        } else if (page == 1 && !selectedTarotType.isEmpty()) {
            return "Выберите конкретный расклад для: " + tarotTypes.get(selectedTarotType);
        } else {
            sendTextMessage(chatId, "Тип расклада не выбран.");
            return "";
        }
    }

    private InlineKeyboardMarkup getMenuMarkup(int page) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();

        if (page == 0) {
            for (Map.Entry<String, String> entry : tarotTypes.entrySet()) {
                buttons.add(Collections.singletonList(createButton(entry.getValue(), "type_" + entry.getKey())));
            }
            buttons.add(Collections.singletonList(createButton("Вперёд ➡", "page_1")));
        } else if (page == 1 && !selectedTarotType.isEmpty()) {
            for (Map.Entry<String, String> entry : tarotLayouts.entrySet()) {
                buttons.add(Collections.singletonList(createButton(entry.getValue(), "layout_" + entry.getKey())));
            }
            buttons.add(Collections.singletonList(createButton("⬅ Назад", "page_0")));
        }

        markup.setKeyboard(buttons);
        return markup;
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton(text);
        button.setCallbackData(callbackData);
        return button;
    }


    public void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode(ParseMode.HTML);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createCardMarkup() {
        InlineKeyboardButton button1 = new InlineKeyboardButton("🔄 Заново");
        button1.setCallbackData("🔄 Заново");

        InlineKeyboardButton button2 = new InlineKeyboardButton("🔮 Проанализировать расклад");
        button2.setCallbackData("🔮 Проанализировать расклад");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        keyboard.add(Collections.singletonList(button1)); // Кнопка в отдельной строке
        keyboard.add(Collections.singletonList(button2)); // Вторая кнопка в отдельной строке

        markup.setKeyboard(keyboard);

        return markup;
    }

    private void sendOrEditMessage(Long chatId, Integer messageId, String text, boolean isEdit) {
        if (isEdit) {
            EditMessageText message = new EditMessageText();
            message.setChatId(chatId);
            message.setMessageId(messageId);
            message.setText(text + " \u200B"); // Невидимый символ для предотвращения ошибки
            message.setParseMode("Markdown");
            message.setReplyMarkup(createCardMarkup());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            message.setParseMode("Markdown");
            message.setReplyMarkup(createCardMarkup());

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendCardButton(long chatId, String text) {
        sendOrEditMessage(chatId, null, text, false);
    }

    public void editCardButton(long chatId, int messageId, String newText) {
        sendOrEditMessage(chatId, messageId, newText, true);
    }


    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}