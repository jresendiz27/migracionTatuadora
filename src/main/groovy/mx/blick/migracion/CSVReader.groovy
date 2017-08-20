package mx.blick.migracion

import com.xlson.groovycsv.CsvIterator
import com.xlson.groovycsv.PropertyMapper

import static com.xlson.groovycsv.CsvParser.parseCsv

@Singleton
class CSVReader {
    List<Map> studios = []
    List<Map> freelancers = []
    List<Map> clients = []

    void loadCSVs() {
        this.loadClients()
        this.loadStudiosAndFreelancers()
    }

    void loadClients() {
        clients = []
        String csvPath = getClass().getResource('/csv/clients.csv').toURI()
        String csvContent = new File(csvPath).text

        CsvIterator data = parseCsv(csvContent) as CsvIterator
        data.each { PropertyMapper line ->
            Map currentRowMap = sanitizeMap(line.toMap())
            clients.add(currentRowMap)
        }
    }

    void loadStudiosAndFreelancers() {
        String csvPath = getClass().getResource('/csv/studios.csv').toURI()
        String csvContent = new File(csvPath).text

        CsvIterator data = parseCsv(csvContent) as CsvIterator
        data.each { PropertyMapper line ->
            Map sanitizedMap = sanitizeMap(line.toMap())
            String studioTypeFromRow = (sanitizedMap.get("userType") as String).toLowerCase().trim()
            if ("studio".equals(studioTypeFromRow)) {
                studios.add(sanitizeMap())
            } else {
                freelancers.add(sanitizeMap())
            }
        }
    }

    List<Map> getStudios() {
        if(this.studios.size() > 0) {
            return studios
        } else {
            this.loadStudiosAndFreelancers()
            return studios
        }
    }

    List<Map> getFreelancers() {
        if(this.freelancers.size() > 0) {
            return freelancers
        } else {
            this.loadStudiosAndFreelancers()
            return freelancers
        }
    }

    List<Map> getClients() {
        if(this.clients.size() > 0) {
            return clients
        } else {
            this.loadClients()
            return clients
        }
    }

    private static Map sanitizeMap(Map currentRowMap) {
        Map sanitizedMap = [:]
        currentRowMap.each { key, value ->
            String stringValue = "${value}".trim()
            try {
                sanitizedMap["${key}"] = stringValue as Double
            } catch (Exception e) {
                sanitizedMap["${key}"] = stringValue
            }
        }
        return sanitizedMap
    }
}