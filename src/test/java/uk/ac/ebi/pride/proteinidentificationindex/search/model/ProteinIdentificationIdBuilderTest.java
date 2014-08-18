package uk.ac.ebi.pride.proteinidentificationindex.search.model;

import org.junit.Test;
import uk.ac.ebi.pride.proteinidentificationindex.search.util.ProteinIdentificationIdBuilder;

import static junit.framework.Assert.assertEquals;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinIdentificationIdBuilderTest {

    private static final String PROTEIN_ACCESSION = "protein-accession-1_2_3_4";
    private static final String PROJECT_ACCESSION = "project-accession-1_2_3_4";
    private static final String ASSAY_ACCESSION = "assay-accession-1_2_3_4";
    private static final String ID =
            PROTEIN_ACCESSION + ProteinIdentificationIdBuilder.SEPARATOR
            + PROJECT_ACCESSION + ProteinIdentificationIdBuilder.SEPARATOR
            + ASSAY_ACCESSION;

    @Test
    public void testGetId() throws Exception {
        String newID = ProteinIdentificationIdBuilder.getId(PROTEIN_ACCESSION,PROJECT_ACCESSION,ASSAY_ACCESSION);
        assertEquals(ID,newID);
    }

    @Test
    public void testGetProteinAccession() throws Exception {
        String proteinAccession = ProteinIdentificationIdBuilder.getProteinAccession(ID);
        assertEquals(PROTEIN_ACCESSION,proteinAccession);
    }

    @Test
    public void testGetProjectAccession() throws Exception {
        String projectAccession = ProteinIdentificationIdBuilder.getProjectAccession(ID);
        assertEquals(PROJECT_ACCESSION,projectAccession);
    }

    @Test
    public void testGetAssayAccession() throws Exception {
        String assayAccession = ProteinIdentificationIdBuilder.getAssayAccession(ID);
        assertEquals(ASSAY_ACCESSION,assayAccession);
    }
}
