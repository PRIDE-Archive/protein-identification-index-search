package uk.ac.ebi.pride.proteinidentificationindex.search.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.repository.SolrProteinIdentificationRepository;

/**
 * @author Jose A. Dianes
 * @version $Id$
 *
 * NOTE: protein accessions can contain chars that produce problems in solr queries ([,],:). They are replaced by _ when
 * using the repository methods
 */
@Service
public class ProteinIdentificationIndexService {

    private static Logger logger = LoggerFactory.getLogger(ProteinIdentificationIndexService.class.getName());

    private SolrProteinIdentificationRepository solrProteinIdentificationRepository;

    public ProteinIdentificationIndexService(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    public void setSolrProteinIdentificationRepository(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
        this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
    }

    public void save(ProteinIdentification proteinIdentification) {
        // fix the accession of needed
        //TODO
//        logger.info("Saving PSM with accession " + psm.getId());
//        psm.setId(PsmIdCleaner.cleanId(psm.getId()));
        solrProteinIdentificationRepository.save(proteinIdentification);
    }

    public void save(Iterable<ProteinIdentification> psms) {
        if (psms==null || !psms.iterator().hasNext())
            logger.info("No Protein Identifications to save");
        else {
            // fix the accession if needed
//        for (Psm psm: psms) {
////            logger.info("Saving PSM with accession " + psm.getId());
//            psm.setId(PsmIdCleaner.cleanId(psm.getId()));
//        }
            solrProteinIdentificationRepository.save(psms);
        }
    }

    public void delete(ProteinIdentification proteinIdentification){
        solrProteinIdentificationRepository.delete(proteinIdentification);
    }

    public void delete(Iterable<ProteinIdentification> psms){
        if (psms==null || !psms.iterator().hasNext())
            logger.info("No Protein Identifications to delete");
        else {
            solrProteinIdentificationRepository.delete(psms);
        }
    }

    public void deleteAll() {
        solrProteinIdentificationRepository.deleteAll();
    }
}
