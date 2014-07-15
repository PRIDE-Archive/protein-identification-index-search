package uk.ac.ebi.pride.proteinidentificationindex.search.model;

import org.apache.solr.client.solrj.beans.Field;
import uk.ac.ebi.pride.archive.dataprovider.identification.ProteinReferenceProvider;

import java.util.List;
import java.util.Set;

/**
 * @author Jose A. Dianes
 * @version $Id$
 */
public class ProteinIdentification implements ProteinReferenceProvider {

    @Field(ProteinIdentificationFields.ACCESSION)
    private String accession;

    @Field(ProteinIdentificationFields.PROJECT_ACCESSION)
    private String projectAccession;

    @Field(ProteinIdentificationFields.ASSAY_ACCESSION)
    private String assayAccession;

    @Field(ProteinIdentificationFields.SYNONYMS)
    private Set<String> synonyms;

    @Field(ProteinIdentificationFields.SEQUENCE)
    private String sequence;

    @Field(ProteinIdentificationFields.DESCRIPTION)
    private List<String> description;

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public Set<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<String> synonyms) {
        this.synonyms = synonyms;
    }

    public String getProjectAccession() {
        return projectAccession;
    }

    public void setProjectAccession(String projectAccession) {
        this.projectAccession = projectAccession;
    }

    public String getAssayAccession() {
        return assayAccession;
    }

    public void setAssayAccession(String assayAccession) {
        this.assayAccession = assayAccession;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

}
