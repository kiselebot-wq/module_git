// Задача 1, вариант 11 «Путевое хозяйство»: Factory Method для дефектов рельсов.
// Клиентский код работает только с абстракциями RailDefectFactory и RailDefect.
import java.util.Arrays;
import java.util.List;

public class FactoryAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {

        // Каталог создателей. Четвёртый создатель добавлен отдельным классом:
        // для этого не потребовалось править RailDefectFactory или первые три создателя.
        List<RailDefectFactory> defectFactories = Arrays.asList(
                new HeadCrackFactory(),
                new WebCrackFactory(),
                new FootCorrosionFactory(),
                new GaugeWideningFactory());

        String[] expectedCodes = {"ГТ", "ТШ", "КП", "УК"};
        String[] expectedClassNames = {
                "HeadCrackDefect", "WebCrackDefect", "FootCorrosionDefect", "GaugeWideningDefect"
        };

        System.out.println("--- Дефекты рельсов, созданные фабричным методом ---");
        for (RailDefectFactory defectFactory : defectFactories) {
            RailDefect defect = defectFactory.createDefect(128.5, 45);
            System.out.printf("%-14s %-30s код=%-3s балл=%d  ограничение=%d км/ч%n",
                    defectFactory.getClass().getSimpleName(),
                    defect.title(), defect.code(), defect.severityLevel(), defect.speedLimitKmH());
        }

        System.out.println();
        System.out.println("--- Проверки ---");
        for (int creatorIndex = 0; creatorIndex < defectFactories.size(); creatorIndex++) {
            RailDefect defect = defectFactories.get(creatorIndex).createDefect(128.5, 45);
            String defectLabel = expectedCodes[creatorIndex];

            check("создатель " + expectedClassNames[creatorIndex] + " вернул нужный продукт",
                    defect.getClass().getSimpleName().equals(expectedClassNames[creatorIndex]));
            check("описание дефекта " + defectLabel + " не пустое и содержит код",
                    !defect.description().isEmpty() && defect.description().contains(defect.code()));
            check("характеристики дефекта " + defectLabel + " сохранены фабрикой",
                    defect.kmMark() == 128.5 && defect.lengthMm() == 45);
            check("оценка опасности дефекта " + defectLabel + " в допустимом диапазоне",
                    defect.severityLevel() >= 1 && defect.severityLevel() <= 4
                            && defect.speedLimitKmH() > 0);
        }

        // Четвёртый тип проверяется точно так же, как и остальные: через общие типы.
        RailDefect fourthDefect = defectFactories.get(3).createDefect(12.0, 80);
        check("четвёртый тип дефекта работает наравне с остальными",
                fourthDefect.code().equals("УК") && fourthDefect.title().contains("колеи")
                        && fourthDefect.lengthMm() == 80);

        finishChecks();
    }

    // --- служебное: проверки и завершение ---

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    static void finishChecks() {
        if (failedChecks > 0) {
            throw new AssertionError(" Factory Method: не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки Factory Method пройдены.");
    }
}

// --- Участник паттерна: продукт ---

/** Общий тип всех дефектов рельсов с описанием и измеримыми характеристиками. */
abstract class RailDefect {

    private final double kmMark;      // километровая отметка места дефекта
    private final int lengthMm;       // протяжённость дефекта, миллиметры

    RailDefect(double kmMark, int lengthMm) {
        this.kmMark = kmMark;
        this.lengthMm = lengthMm;
    }

    /** Код дефекта в журнале путевого хозяйства. */
    abstract String code();

    /** Человекочитаемое название дефекта. */
    abstract String title();

    /** Балл опасности по шкале от 1 до 4. */
    abstract int severityLevel();

    /** Ограничение скорости на участке, км/ч. */
    abstract int speedLimitKmH();

    final double kmMark() {
        return kmMark;
    }

    final int lengthMm() {
        return lengthMm;
    }

    /** Полное описание дефекта, используемое клиентским кодом. */
    final String description() {
        return title() + " [" + code() + "], пикет " + kmMark + " км, длина " + lengthMm
                + " мм, балл опасности " + severityLevel()
                + ", ограничение скорости " + speedLimitKmH() + " км/ч";
    }
}

// --- Участник паттерна: создатель ---

/** Абстрактная фабрика: фабричный метод возвращает только общий тип продукта. */
abstract class RailDefectFactory {

    abstract RailDefect createDefect(double kmMark, int lengthMm);
}

// --- Первые три продукта и их создатели ---

/** Головная трещина: наиболее опасный дефект головки рельса. */
class HeadCrackDefect extends RailDefect {

    HeadCrackDefect(double kmMark, int lengthMm) {
        super(kmMark, lengthMm);
    }

    String code() {
        return "ГТ";
    }

    String title() {
        return "Головная трещина рельса";
    }

    int severityLevel() {
        return 4;
    }

    int speedLimitKmH() {
        return 25;
    }
}

class HeadCrackFactory extends RailDefectFactory {

    RailDefect createDefect(double kmMark, int lengthMm) {
        return new HeadCrackDefect(kmMark, lengthMm);
    }
}

/** Трещина в шейке рельса: требует усиления и контроля. */
class WebCrackDefect extends RailDefect {

    WebCrackDefect(double kmMark, int lengthMm) {
        super(kmMark, lengthMm);
    }

    String code() {
        return "ТШ";
    }

    String title() {
        return "Трещина в шейке рельса";
    }

    int severityLevel() {
        return 3;
    }

    int speedLimitKmH() {
        return 40;
    }
}

class WebCrackFactory extends RailDefectFactory {

    RailDefect createDefect(double kmMark, int lengthMm) {
        return new WebCrackDefect(kmMark, lengthMm);
    }
}

/** Коррозия подошвы: развивается медленно, но снижает устойчивость рельса. */
class FootCorrosionDefect extends RailDefect {

    FootCorrosionDefect(double kmMark, int lengthMm) {
        super(kmMark, lengthMm);
    }

    String code() {
        return "КП";
    }

    String title() {
        return "Коррозия подошвы рельса";
    }

    int severityLevel() {
        return 2;
    }

    int speedLimitKmH() {
        return 60;
    }
}

class FootCorrosionFactory extends RailDefectFactory {

    RailDefect createDefect(double kmMark, int lengthMm) {
        return new FootCorrosionDefect(kmMark, lengthMm);
    }
}

// --- ЧЕТВЁРТЫЙ ТИП: добавлен новыми классами, прежние классы не изменялись ---

/** Уширение колеи: дефект пути, влияющий на сход с рельса. */
class GaugeWideningDefect extends RailDefect {

    GaugeWideningDefect(double kmMark, int lengthMm) {
        super(kmMark, lengthMm);
    }

    String code() {
        return "УК";
    }

    String title() {
        return "Уширение колеи";
    }

    int severityLevel() {
        return 3;
    }

    int speedLimitKmH() {
        return 50;
    }
}

class GaugeWideningFactory extends RailDefectFactory {

    RailDefect createDefect(double kmMark, int lengthMm) {
        return new GaugeWideningDefect(kmMark, lengthMm);
    }
}
