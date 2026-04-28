package Resources.Data;

import Resources.Emergency.EmergencyCenter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonDataLoader {

    public static List<EmergencyCenter> loadCenters(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            return parseCenters(br);
        } catch (Exception e) {
            System.out.println("Error leyendo JSON: " + e.getMessage());
        }

        return new ArrayList<>();
    }

    public static List<EmergencyCenter> loadCentersFromResource(String resourcePath) {
        try (InputStream inputStream = JsonDataLoader.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.out.println("No se encontro el recurso JSON: " + resourcePath);
                return new ArrayList<>();
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                return parseCenters(br);
            }
        } catch (Exception e) {
            System.out.println("Error leyendo recurso JSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static List<EmergencyCenter> parseCenters(BufferedReader br) throws Exception {
        List<EmergencyCenter> centers = new ArrayList<>();
        String line;
        String name = "";
        String type = "";
        double lat = 0;
        double lon = 0;

        while ((line = br.readLine()) != null) {
            line = line.trim();

            if (line.contains("\"name\"")) {
                name = extractString(line);
            }
            if (line.contains("\"type\"")) {
                type = extractString(line);
            }
            if (line.contains("\"latitude\"")) {
                lat = extractDouble(line);
            }
            if (line.contains("\"longitude\"")) {
                lon = extractDouble(line);
            }

            if (!name.isEmpty() && !type.isEmpty() && lat != 0 && lon != 0) {
                centers.add(new EmergencyCenter(name, type, lat, lon));
                name = "";
                type = "";
                lat = 0;
                lon = 0;
            }
        }

        return centers;
    }

    private static String extractString(String line) {
        return line.split(":")[1].replace("\"", "").replace(",", "").trim();
    }

    private static double extractDouble(String line) {
        return Double.parseDouble(line.split(":")[1].replace(",", "").trim());
    }
}
