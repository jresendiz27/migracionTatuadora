package mx.blick.migracion

import com.xlson.groovycsv.CsvIterator
import com.xlson.groovycsv.PropertyMapper
import groovy.util.logging.Log4j

import static com.xlson.groovycsv.CsvParser.parseCsv

@Singleton
@Log4j
class CSVReader {
    List<Map> studios = []
    List<Map> freelancers = []
    List<Map> clients = []
    List<Map> artists = []
    List<Map> tattoos = []

    void loadClients() {
        String csvPath = getClass().getResource('/csv/clients.csv').getFile()
        String csvContent = new File(csvPath).text

        CsvIterator data = parseCsv(csvContent) as CsvIterator
        data.each { PropertyMapper line ->
            Map currentRowMap = sanitizeMap(line.toMap())
            clients.add(currentRowMap)
        }
    }

    void loadStudiosAndFreelancers() {
        String csvPath = getClass().getResource('/csv/estudios.csv').getFile()
        String csvContent = new File(csvPath).text

        CsvIterator data = parseCsv(csvContent) as CsvIterator
        data.each { PropertyMapper line ->
            Map map = sanitizeMap(line.toMap())
            String studioTypeFromRow = (map.get("userType") as String)?.toLowerCase()?.trim()
            if ("studio" == studioTypeFromRow) {
                studios.add(map)
            } else {
                freelancers.add(map)
            }
        }
    }

    void loadArtists() {
        String csvPath = getClass().getResource('/csv/artists.csv').getFile()
        String csvContent = new File(csvPath).text

        CsvIterator data = parseCsv(csvContent) as CsvIterator
        data.each { PropertyMapper line ->
            Map artistSanitizedMap = sanitizeMap(line.toMap())
            artists.add(artistSanitizedMap)
        }
    }

    void loadTattoos() {
        String csvPath = getClass().getResource('/csv/images.csv').getFile()
        String csvContent = new File(csvPath).text

        CsvIterator data = parseCsv(csvContent) as CsvIterator
        data.each { PropertyMapper line ->
            Map map = sanitizeMap(line.toMap())
            String studioTypeFromRow = (map.get("userType") as String)?.toLowerCase()?.trim()
            tattoos.add(map)
        }
    }

    private static Map sanitizeMap(Map currentRowMap) {
        Map map = [:]
        currentRowMap.each { key, value ->
            String stringValue = "${value}".trim()
            if (stringValue.toLowerCase() in ["true", "false"]) {
                map["${key}"] = "true".equalsIgnoreCase(stringValue) ? 1 : 0
            } else {
                map["${key}"] = stringValue
            }
        }
        return map
    }
}