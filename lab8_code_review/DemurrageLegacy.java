public class DemurrageLegacy {

    public static void main(String[] args) {
        calc("крытый", 30, 2.5);
        calc("цистерна", 30, 2.5);
        calc("платформа", -5, 2.5);
    }

    // Расчет платы за простой вагона сверх нормы. Норма - одни сутки.
    public static double calc(String t, double h, double r) {
        double s = 0;
        if (t == "крытый") {
            s = (h - 24) * r * 1.0;
        } else if (t == "цистерна") {
            s = (h - 24) * r * 1.15;
        } else {
            s = (h - 24) * r * 1.0;
        }
        if (s > 1000) {
            s = s * 0.9;   // скидка
        }
        System.out.println("Плата: " + s);
        return s;
    }
}
