import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShiftPlanLegacy {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    static List<String> log = new ArrayList<>();

    public static void main(String[] args) {
        List<Brigade> brigades = new ArrayList<>();
        brigades.add(new Brigade("Иванов", 11.0, true));
        brigades.add(new Brigade("Петров", 0.0, true));
        brigades.add(new Brigade("Сидоров", 4.5, false));
        brigades.add(new Brigade("Козлов", 8.0, true));

        System.out.println(make(brigades, 3));
        System.out.println("Журнал: " + log);
    }

    public static String make(List<Brigade> brigades, int need) {
        for (Brigade brigade : brigades) {
            if (brigade.hoursPerShift > 10.0 || brigade.admitted == false) {
                brigades.remove(brigade);
            }
        }

        String result = "";
        for (int i = 0; i < need; i++) {
            result = result + brigades.get(i).lastName + "; ";
        }

        try {
            FileWriter writer = new FileWriter("наряд.txt");
            writer.write(result);
        } catch (Exception e) {
        }

        log.add(dateFormat.format(new Date()) + " наряд сформирован");
        return result;
    }
}

class Brigade {
    String lastName;
    double hoursPerShift;
    boolean admitted;

    Brigade(String lastName, double hoursPerShift, boolean admitted) {
        this.lastName = lastName;
        this.hoursPerShift = hoursPerShift;
        this.admitted = admitted;
    }
}
