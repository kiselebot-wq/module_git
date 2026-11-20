// Задача 2, вариант 11 «Путевое хозяйство»: Observer для события «обнаружен дефект рельса».
// Субъект не знает конкретных наблюдателей: он работает только с интерфейсом DefectListener.
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObserverAfter {

    private static int failedChecks = 0;

    public static void main(String[] args) {

        TrackSection trackSection = new TrackSection("Перегон Гомель — Жлобин, 128 км");
        RepairServiceListener repairService = new RepairServiceListener();
        DispatcherListener dispatcher = new DispatcherListener();
        DefectLogListener defectLog = new DefectLogListener();
        AsuTransmissionListener asuTransmission = new AsuTransmissionListener();

        // Подписка: субъект получает только объекты общего интерфейса.
        trackSection.addListener(repairService);
        trackSection.addListener(dispatcher);
        trackSection.addListener(defectLog);
        trackSection.addListener(asuTransmission);

        System.out.println("--- Первое событие: дефект обнаружен ---");
        trackSection.defectDetected("ГТ", 128.5, 4);
        List<String> firstDeliveryOrder = new ArrayList<>(trackSection.deliveryOrder());
        System.out.println("Порядок получения события: " + firstDeliveryOrder);

        DefectEvent expectedEvent = new DefectEvent(
                "Перегон Гомель — Жлобин, 128 км", "ГТ", 128.5, 4);

        System.out.println();
        System.out.println("--- Проверки первого события ---");
        check("все четыре подписчика получили событие",
                repairService.receivedCount() == 1 && dispatcher.receivedCount() == 1
                        && defectLog.messages().size() == 1 && asuTransmission.receivedCount() == 1);
        check("служба ремонта и диспетчер получили одно и то же событие",
                expectedEvent.equals(repairService.lastEvent())
                        && expectedEvent.equals(dispatcher.lastEvent()));
        check("журнал сохранил сообщение с кодом дефекта",
                defectLog.messages().size() == 1 && defectLog.messages().get(0).contains("ГТ"));
        check("четвёртый наблюдатель (АСУ) работает без правки TrackSection",
                expectedEvent.equals(asuTransmission.lastEvent())
                        && asuTransmission.name().contains("АСУ"));
        check("порядок уведомления совпадает с порядком подписки",
                firstDeliveryOrder.equals(Arrays.asList(
                        "Служба ремонта", "Диспетчер", "Журнал дефектов", "Передача в АСУ")));

        // Отписка диспетчера: после неё события ему больше не приходят.
        System.out.println();
        System.out.println("--- Отписка диспетчера и второе событие ---");
        trackSection.removeListener(dispatcher);
        check("количество подписчиков уменьшилось после отписки", trackSection.listenerCount() == 3);
        trackSection.defectDetected("ТШ", 129.2, 3);
        System.out.println("Порядок получения второго события: " + trackSection.deliveryOrder());

        System.out.println();
        System.out.println("--- Проверки после отписки ---");
        check("отписанный диспетчер не получил второе событие", dispatcher.receivedCount() == 1);
        check("активные подписчики получили второе событие",
                repairService.receivedCount() == 2 && defectLog.messages().size() == 2
                        && asuTransmission.receivedCount() == 2);
        check("журнал накопил две записи и сохранил новый код дефекта",
                defectLog.messages().size() == 2 && defectLog.messages().get(1).contains("ТШ"));

        // Повторная подписка показывает, что механизм регистрации обратим.
        trackSection.addListener(dispatcher);
        trackSection.defectDetected("КП", 130.0, 2);
        check("повторная подписка восстановила получение событий", dispatcher.receivedCount() == 2);

        finishChecks();
    }

    // --- служебное ---

    static void check(String checkName, boolean passed) {
        System.out.println((passed ? "[OK]   " : "[FAIL] ") + checkName);
        if (!passed) {
            failedChecks++;
        }
    }

    static void finishChecks() {
        if (failedChecks > 0) {
            throw new AssertionError(" Observer: не пройдено проверок: " + failedChecks);
        }
        System.out.println();
        System.out.println("Все проверки Observer пройдены.");
    }
}

// --- Субъект: знает только интерфейс наблюдателя ---

final class DefectEvent {

    private final String sectionName;
    private final String defectCode;
    private final double kmMark;
    private final int severityLevel;

    DefectEvent(String sectionName, String defectCode, double kmMark, int severityLevel) {
        this.sectionName = sectionName;
        this.defectCode = defectCode;
        this.kmMark = kmMark;
        this.severityLevel = severityLevel;
    }

    String sectionName() {
        return sectionName;
    }

    String defectCode() {
        return defectCode;
    }

    double kmMark() {
        return kmMark;
    }

    int severityLevel() {
        return severityLevel;
    }

    String text() {
        return "код " + defectCode + ", пикет " + kmMark + " км, балл " + severityLevel
                + ", участок «" + sectionName + "»";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DefectEvent)) {
            return false;
        }
        DefectEvent otherEvent = (DefectEvent) other;
        return Double.compare(kmMark, otherEvent.kmMark) == 0
                && severityLevel == otherEvent.severityLevel
                && sectionName.equals(otherEvent.sectionName)
                && defectCode.equals(otherEvent.defectCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sectionName, defectCode, kmMark, severityLevel);
    }
}

/** Интерфейс наблюдателя: метод обработки обнаруженного дефекта. */
interface DefectListener {

    void onDefectFound(DefectEvent event);

    /** Имя используется только для демонстрации порядка уведомления. */
    String name();
}

/** Субъект: участок пути, на котором обнаружен дефект. */
class TrackSection {

    private final String sectionName;
    private final List<DefectListener> listeners = new ArrayList<>();
    private final List<String> deliveryOrder = new ArrayList<>();

    TrackSection(String sectionName) {
        this.sectionName = sectionName;
    }

    void addListener(DefectListener listener) {
        listeners.add(listener);
    }

    void removeListener(DefectListener listener) {
        listeners.remove(listener);
    }

    /** Оповещает всех подписанных наблюдателей; о их конкретных типах ничего не знает. */
    void defectDetected(String defectCode, double kmMark, int severityLevel) {
        DefectEvent event = new DefectEvent(sectionName, defectCode, kmMark, severityLevel);
        deliveryOrder.clear();
        for (DefectListener listener : listeners) {
            listener.onDefectFound(event);
            deliveryOrder.add(listener.name());
        }
    }

    List<String> deliveryOrder() {
        return Collections.unmodifiableList(deliveryOrder);
    }

    int listenerCount() {
        return listeners.size();
    }
}

// --- Первые три наблюдателя ---

class RepairServiceListener implements DefectListener {

    private int receivedCount;
    private DefectEvent lastEvent;

    public void onDefectFound(DefectEvent event) {
        receivedCount++;
        lastEvent = event;
        System.out.println("[Служба ремонта]   Наряд-допуск на устранение: " + event.text());
    }

    public String name() {
        return "Служба ремонта";
    }

    int receivedCount() {
        return receivedCount;
    }

    DefectEvent lastEvent() {
        return lastEvent;
    }
}

class DispatcherListener implements DefectListener {

    private int receivedCount;
    private DefectEvent lastEvent;

    public void onDefectFound(DefectEvent event) {
        receivedCount++;
        lastEvent = event;
        System.out.println("[Диспетчер]        Ограничение скорости на участке: " + event.text());
    }

    public String name() {
        return "Диспетчер";
    }

    int receivedCount() {
        return receivedCount;
    }

    DefectEvent lastEvent() {
        return lastEvent;
    }
}

class DefectLogListener implements DefectListener {

    private final List<String> messages = new ArrayList<>();

    public void onDefectFound(DefectEvent event) {
        String message = "[Журнал дефектов] Записано: " + event.text();
        messages.add(message);
        System.out.println(message);
    }

    public String name() {
        return "Журнал дефектов";
    }

    List<String> messages() {
        return Collections.unmodifiableList(messages);
    }
}

// --- ЧЕТВЁРТЫЙ НАБЛЮДАТЕЛЬ: добавлен новым классом, TrackSection не изменялся ---

class AsuTransmissionListener implements DefectListener {

    private int receivedCount;
    private DefectEvent lastEvent;

    public void onDefectFound(DefectEvent event) {
        receivedCount++;
        lastEvent = event;
        System.out.println("[Передача в АСУ]   Пакет в автоматизированную систему: " + event.text());
    }

    public String name() {
        return "Передача в АСУ";
    }

    int receivedCount() {
        return receivedCount;
    }

    DefectEvent lastEvent() {
        return lastEvent;
    }
}
