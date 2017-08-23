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
        thisInstance.databaseTool.truncateAllRelationships()

        loadStudiosAndFreelancers(thisInstance)
        loadClients(thisInstance)
    }

    static void loadStudiosAndFreelancers(MigrateStudiosAndFreelancers thisInstance) {
        Map studioEmailMap = [:]
        Map freelanceEmailMap = [:]
        Map artistEmailMap = [:]

        thisInstance.csvReader.loadStudiosAndFreelancers()
        thisInstance.csvReader.loadArtists()
        thisInstance.csvReader.loadTattoos()

        List<Map> studios = thisInstance.csvReader.studios.unique { studioMap -> studioMap.email }
        List<Map> freelancers = thisInstance.csvReader.freelancers.unique { freelancerMap -> freelancerMap.email }
        List<Map> artists = thisInstance.csvReader.artists
        List<Map> tattooImages = thisInstance.csvReader.tattoos

        // TODO associate tattoos images with studios and freelancers, upload them to the server?
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
            if (artistEmailMap.get("${artistMap.studioEmail?.trim()}")) {
                List artistsIds = artistEmailMap.get("${artistMap.studioEmail?.trim()}")
                artistsIds.add(artistId)
                artistEmailMap.put("${artistMap.studioEmail?.trim()}", artistsIds)
            } else {
                artistEmailMap.put("${artistMap.studioEmail?.trim()}", [artistId])
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

        tattooImages.each { tattooMap ->
            Random random = new Random()
            Integer artistId = null
            Integer freelanceId = null
            if (artistEmailMap.get("${tattooMap.studioEmail?.trim()}")) {
                List ids = artistEmailMap.get("${tattooMap.studioEmail?.trim()}") as List
                Integer randomIndex = random.nextInt(ids.size())
                artistId = ids[randomIndex] as Integer
            }

            if (freelanceEmailMap.get("${tattooMap.studioEmail?.trim()}")) {
                freelanceId = freelanceEmailMap.get("${tattooMap.studioEmail?.trim()}").freelanceId as Integer
            }

            tattooMap.artist = artistId
            tattooMap.freelancer = freelanceId

            if (artistId || freelanceId) {
                Integer tattooId = thisInstance.databaseTool.insertTattoo(tattooMap).first()[0] as Integer

                if (tattooMap.styles) {
                    tattooMap.styles.split(",").each { style ->
                        String sanitizedStyle = StringUtil.capitalizeString(StringUtil.sanitizeString(style as String))
                        Integer styleId = Constants.UNIQUE_STYLES.get(sanitizedStyle)
                        if (styleId) {
                            thisInstance.databaseTool.associateTattooWithStyle(tattooId, styleId)
                        }
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
