import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShiftPlanFixed {

    static final int MAX_HOURS_PER_SHIFT = 10;
    static final String OUTPUT_FILE = "наряд.txt";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    static final List<String> LOG = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        List<Brigade> brigades = List.of(
                new Brigade("Иванов", 11.0, true),
                new Brigade("Петров", 0.0, true),
                new Brigade("Сидоров", 4.5, false),
                new Brigade("Козлов", 8.0, true));

        // Корректные данные: тот же состав наряда, что и в исходной версии.
        System.out.println(make(brigades, 2));

        // Три бригады на смену при двух допущенных — осмысленный отказ.
        try {
            make(brigades, 3);
        } catch (IllegalArgumentException e) {
            System.out.println("Ожидаемый отказ: " + e.getMessage());
        }
        System.out.println("Журнал: " + LOG);

        runSelfChecks();
    }

    public record Brigade(String lastName, double hoursPerShift, boolean admitted) {
        public Brigade {
            if (lastName == null || lastName.isBlank()) {
                throw new IllegalArgumentException("Фамилия бригадира не задана");
            }
            if (hoursPerShift < 0 || Double.isNaN(hoursPerShift) || Double.isInfinite(hoursPerShift)) {
                throw new IllegalArgumentException("Часы смены должны быть неотрицательным числом");
            }
        }
    }

    public static String make(List<Brigade> brigades, int need) {
        if (brigades == null) {
            throw new IllegalArgumentException("Список бригад не задан");
        }
        if (need < 1) {
            throw new IllegalArgumentException("Число бригад на смену должно быть положительным: " + need);
        }
        List<Brigade> eligible = new ArrayList<>();
        for (Brigade brigade : brigades) {
            if (brigade == null) {
                throw new IllegalArgumentException("Список бригад содержит пустой элемент");
            }
            if (brigade.hoursPerShift() <= MAX_HOURS_PER_SHIFT && brigade.admitted()) {
                eligible.add(brigade);
            }
        }
        if (eligible.size() < need) {
            throw new IllegalArgumentException("Недостаточно допущенных бригад: нужно "
                    + need + ", доступно " + eligible.size());
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < need; i++) {
            result.append(eligible.get(i).lastName()).append("; ");
        }
        String text = result.toString();

        try (FileWriter writer = new FileWriter(OUTPUT_FILE, StandardCharsets.UTF_8)) {
            writer.write(text);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось записать наряд в файл " + OUTPUT_FILE, e);
        }

        LOG.add(LocalDate.now().format(DATE_FORMAT) + " наряд сформирован");
        return text;
    }

    static List<Brigade> validBrigades() {
        return Arrays.asList(
                new Brigade("Иванов", 8.0, true),
                new Brigade("Петров", 7.5, true),
                new Brigade("Сидоров", 4.5, true));
    }

    static void check(String name, boolean ok) {
        if (ok) {
            System.out.println("[OK] " + name);
        } else {
            System.out.println("[FAIL] " + name);
            throw new AssertionError(name);
        }
    }

    static void checkThrows(String name, Runnable action, String messagePart) {
        try {
            action.run();
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage() != null && e.getMessage().contains(messagePart));
        }
    }

    static void runSelfChecks() {
        check("Характеризующий тест: корректные данные дают прежний наряд",
                "Иванов; Петров; ".equals(make(validBrigades(), 2)));

        check("Отсев по часам и допуску",
                "Петров; Козлов; ".equals(make(List.of(
                        new Brigade("Иванов", 11.0, true),
                        new Brigade("Петров", 0.0, true),
                        new Brigade("Сидоров", 4.5, false),
                        new Brigade("Козлов", 8.0, true)), 2)));

        List<Brigade> source = new ArrayList<>(validBrigades());
        make(source, 1);
        check("Метод не изменяет переданный список", source.size() == 3);

        checkThrows("Недостаточно допущенных бригад",
                () -> make(validBrigades(), 4), "Недостаточно допущенных бригад");
        checkThrows("Отрицательное число бригад",
                () -> make(validBrigades(), -1), "положительным");
        checkThrows("Нулевое число бригад",
                () -> make(validBrigades(), 0), "положительным");
        checkThrows("Null вместо списка",
                () -> make(null, 1), "не задан");

        String written = make(validBrigades(), 2);
        check("Файл содержит сформированный наряд", readOutput().equals(written));
        check("Журнал пополняется только после записи файла",
                !LOG.isEmpty() && LOG.get(LOG.size() - 1).endsWith(" наряд сформирован"));

        checkThrows("Пустая фамилия отклоняется",
                () -> new Brigade("", 8.0, true), "Фамилия");
        checkThrows("Отрицательные часы отклоняются",
                () -> new Brigade("Иванов", -2.0, true), "неотрицательным");

        int before = LOG.size();
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Thread thread = new Thread(() -> {
                try {
                    if (!"Иванов; ".equals(make(validBrigades(), 1))) {
                        errors.add("неверный наряд");
                    }
                } catch (RuntimeException e) {
                    errors.add(e.getClass().getSimpleName());
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        check("Параллельные вызовы не ломают состояние", errors.isEmpty() && LOG.size() == before + 8);
    }

    static String readOutput() {
        try {
            return Files.readString(Path.of(OUTPUT_FILE), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл наряда", e);
        }
    }
}
