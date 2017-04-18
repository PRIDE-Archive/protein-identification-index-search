package uk.ac.ebi.pride.proteinidentificationindex.search.service;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.pride.proteinidentificationindex.search.model.ProteinIdentification;
import uk.ac.ebi.pride.proteinidentificationindex.search.service.repository.SolrProteinIdentificationRepository;

import java.util.Collection;
import java.util.LinkedList;

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

  private static final int NUM_TRIES = 10;
  private static final int SECONDS_TO_WAIT = 30;
  private static final long MAX_ELAPSED_TIME_PING_QUERY = 10000;

  private SolrServer proteinIdentificationServer;

  private SolrProteinIdentificationRepository solrProteinIdentificationRepository;

  public ProteinIdentificationIndexService(SolrServer proteinIdentificationServer, SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
    this.proteinIdentificationServer = proteinIdentificationServer;
    this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
  }

  public void setSolrProteinIdentificationRepository(SolrProteinIdentificationRepository solrProteinIdentificationRepository) {
    this.solrProteinIdentificationRepository = solrProteinIdentificationRepository;
  }

  public boolean save(ProteinIdentification proteinIdentification) {
    Collection<ProteinIdentification> proteinIdentifications = new LinkedList<>();
    proteinIdentifications.add(proteinIdentification);
    return save(proteinIdentifications);
  }

  public boolean save(Collection<ProteinIdentification> proteinIdentifications) {
    if (proteinIdentifications!= null && proteinIdentifications.size()>0) {
      int numTries = 0;
      boolean succeed = false;
      while (numTries < NUM_TRIES) {
        try {
          SolrPingResponse pingResponse = this.proteinIdentificationServer.ping();
          if ((pingResponse.getStatus() == 0) && pingResponse.getElapsedTime() < MAX_ELAPSED_TIME_PING_QUERY) {
            this.proteinIdentificationServer.addBeans(proteinIdentifications);
            this.proteinIdentificationServer.commit();
            succeed = true;
            break;
          } else {
            logger.error("[TRY " + numTries + " Solr server too busy!");
            logger.error("PING response status: " + pingResponse.getStatus());
            logger.error("PING elapsed time: " + pingResponse.getElapsedTime());
            logger.error("Re-trying in " + SECONDS_TO_WAIT + " seconds...");
            waitSecs();
          }
        } catch (Exception e) {
          logger.error("[TRY " + numTries + "] There are server problems: ", e);
          logger.error("Re-trying in " + SECONDS_TO_WAIT + " seconds...");
          waitSecs();
        }
        numTries++;
      }
      return succeed;
    } else {
      logger.error("Protein Identification Index Service [reliable-save]: Trying to save an empty protein list!");
      return false;
    }
  }

  public void delete(ProteinIdentification proteinIdentification){
    solrProteinIdentificationRepository.delete(proteinIdentification);
  }

  public void delete(Iterable<ProteinIdentification> proteinIdentifications){
    if (proteinIdentifications ==null || !proteinIdentifications.iterator().hasNext())
      logger.info("No Protein Identifications to delete");
    else {
      solrProteinIdentificationRepository.delete(proteinIdentifications);
    }
  }

  public void deleteAll() {
    solrProteinIdentificationRepository.deleteAll();
  }

  private void waitSecs() {
    try {
      Thread.sleep(SECONDS_TO_WAIT * 1000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
  }
}