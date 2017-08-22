package mx.blick.migracion

import groovy.util.logging.Log4j

@Log4j
class MigrateStudiosAndFreelancers {
    DatabaseTool databaseTool = DatabaseTool.instance
    CSVReader csvReader = CSVReader.instance

    static void main(def args) {
        MigrateStudiosAndFreelancers thisInstance = new MigrateStudiosAndFreelancers()

        thisInstance.databaseTool.truncateUser()
        thisInstance.databaseTool.truncateStudio()
        thisInstance.databaseTool.truncateFreelance()
        thisInstance.databaseTool.truncateArtists()
        thisInstance.databaseTool.truncateAddress()
        thisInstance.databaseTool.truncateAllStylesRelationships()

        loadStudiosAndFreelancers(thisInstance)
        loadClients(thisInstance)
    }

    static void loadStudiosAndFreelancers(MigrateStudiosAndFreelancers thisInstance) {
        Map studioEmailMap = [:]
        Map freelanceEmailMap = [:]

        thisInstance.csvReader.loadStudiosAndFreelancers()
        thisInstance.csvReader.loadArtists()

        List<Map> studios = thisInstance.csvReader.studios.unique { studioMap -> studioMap.email }
        List<Map> freelancers = thisInstance.csvReader.freelancers.unique { freelancerMap -> freelancerMap.email }
        List<Map> artists = thisInstance.csvReader.artists

        // TODO associate artists with studios
        // TODO associate tattoos images with studios and freelancers, upload them to the server?
        // TODO create an inmemory map to know the user/studio/freelancer id based on the email
        studios.each { studioMap ->
            Integer createdAddressId = thisInstance.databaseTool.createAddress(studioMap).first()[0] as Integer
            studioMap.addressId = createdAddressId
            Integer createdUserId = thisInstance.databaseTool.insertUserStudio(studioMap).first()[0] as Integer
            Integer createdStudioId = thisInstance.databaseTool.createStudio(createdUserId, studioMap).first()[0] as Integer
            studioEmailMap.put("${studioMap.email}", [userId: createdUserId, studioId: createdStudioId])
            if (studioMap.styles) {
                studioMap.styles.split(",").each { style ->
                    String sanitizedStyle = StringUtil.capitalizeString(StringUtil.sanitizeString(style as String))
                    Integer styleId = Constants.UNIQUE_STYLES.get(sanitizedStyle)
                    if (styleId) {
                        thisInstance.databaseTool.associateStudioWithStyle(createdStudioId, styleId)
                    }
                }
            }
        }

        artists.each { artistMap ->
            Integer studioId = studioEmailMap.get("${artistMap.studioEmail?.trim()}").studioId as Integer
            Integer artistId = thisInstance.databaseTool.createArtist(studioId, artistMap).first()[0] as Integer
            if (artistMap.styles) {
                artistMap.styles.split(",").each { style ->
                    String sanitizedStyle = StringUtil.capitalizeString(StringUtil.sanitizeString(style as String))
                    Integer styleId = Constants.UNIQUE_STYLES.get(sanitizedStyle)
                    if (styleId) {
                        thisInstance.databaseTool.associateArtistWithStyle(artistId, styleId)
                    }
                }
            }

        }


        freelancers.each { freelancerMap ->
            Integer createdAddressId = thisInstance.databaseTool.createAddress(freelancerMap).first()[0] as Integer
            freelancerMap.addressId = createdAddressId
            Integer createdUserId = thisInstance.databaseTool.insertUserFreelance(freelancerMap).first()[0] as Integer
            Integer createdFreelanceId = thisInstance.databaseTool.createFreelance(createdUserId, freelancerMap).first()[0] as Integer
            freelanceEmailMap.put("${freelancerMap.email}", [userId: createdUserId, freelanceId: createdFreelanceId])
            if (freelancerMap.styles) {
                freelancerMap.styles.split(",").each { style ->
                    String sanitizedStyle = StringUtil.capitalizeString(StringUtil.sanitizeString(style as String))
                    Integer styleId = Constants.UNIQUE_STYLES.get(sanitizedStyle)
                    if (styleId) {
                        thisInstance.databaseTool.associateFreelanceWithStyle(createdFreelanceId, styleId)
                    }
                }
            }

        }
    }

    static void loadClients(MigrateStudiosAndFreelancers thisInstance) {
        thisInstance.csvReader.loadClients()

        List<Map> clients = thisInstance.csvReader.clients.unique { studioMap -> studioMap.email }

        clients.each { clientMap ->
            Long createdAddressId = thisInstance.databaseTool.createAddress(clientMap).first()[0] as Long
            clientMap.addressId = createdAddressId
            thisInstance.databaseTool.insertUserClient(clientMap)
        }
    }
}
