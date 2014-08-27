package uk.ac.ebi.pride.proteinidentificationindex.search.model;

import org.springframework.data.rest.core.config.Projection;

/**
 * @author florian@ebi.ac.uk.
 */
@Projection(name = "summary", types = ProteinIdentification.class)
public interface ProteinIdentificationSummary {

    String getAccession();
}
