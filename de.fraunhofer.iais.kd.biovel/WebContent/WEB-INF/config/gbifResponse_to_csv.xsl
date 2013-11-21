<?xml version="1.0" encoding="UTF-8"?>
<!--
     XSLT for GBIFResponse to CSV format

     Maps GBIFResponse elements to CSV header elements,
     TaxonConcept : http://rs.tdwg.org/ontology/voc/TaxonConcept
     TaxonOccurrence : http://rs.tdwg.org/ontology/voc/TaxonOccurrence
     TaxonName : http://rs.tdwg.org/ontology/voc/TaxonName 

     To be used to generate input test cases for the Taxonomy Data Cleaning Workflow 
     -->

<xsl:stylesheet version="1.0"    
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
  xmlns:tax="http://rs.tdwg.org/ontology/voc/TaxonOccurrence#" 
  xmlns:tax1="http://rs.tdwg.org/ontology/voc/TaxonConcept#" 
  xmlns:tax2="http://rs.tdwg.org/ontology/voc/TaxonName#"
  xmlns:gbif="http://portal.gbif.org/ws/response/gbif">
  <xsl:output method="text"/>

  
  <xsl:variable name="newline">
    <xsl:text>&#xa;</xsl:text>
  </xsl:variable>

  <xsl:variable name="singlespace">
    <xsl:text> </xsl:text>
  </xsl:variable>


  <xsl:template name="escape-quotes">
  <xsl:param name="text"/>
  <xsl:param name="replace"/>
  <xsl:param name="by"/>
  <xsl:choose>
    <xsl:when test="contains($text,$replace)">
      <xsl:value-of select="substring-before($text,$replace)"/>
      <xsl:value-of select="$by"/>
      <xsl:call-template name="escape-quotes">
        <xsl:with-param name="text" select="substring-after($text,$replace)"/>
        <xsl:with-param name="replace" select="$replace"/>
        <xsl:with-param name="by" select="$by"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$text"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="wrap-when-comma">
  <xsl:param name="unwrappedtext"/>
  <xsl:param name="delimiter"/>
  <xsl:choose>
    <xsl:when test="contains($unwrappedtext,$delimiter)">      
    <xsl:text>"</xsl:text>
    <xsl:call-template name="escape-quotes">
      <xsl:with-param name="text" select="$unwrappedtext"/>
      <xsl:with-param name="replace" select="'&quot;'"/>
      <xsl:with-param name="by" select="'&quot;&quot;'"/>
    </xsl:call-template>      
    <xsl:text>"</xsl:text>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="$unwrappedtext"/>   
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

  <xsl:template match="/"> 
  <xsl:apply-templates select="//tax:TaxonOccurrence"/>        
</xsl:template>

<xsl:template match="tax:TaxonOccurrence">    

<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="./tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:TaxonName/tax2:authorship/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:genusPart/text()"/>   
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>
    
<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:infragenericEpithet/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:specificEpithet/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:infraspecificEpithet/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="./tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:nameComplete/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:identifiedTo/tax:Identification/tax:taxon/tax1:TaxonConcept/tax1:hasName/tax2:TaxonName/tax2:uninomial/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:identifiedTo/tax:Identification/tax:taxonName/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>  
<xsl:call-template name="wrap-when-comma">
 <xsl:with-param name="unwrappedtext" select="@gbifKey"/>
 <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:decimalLatitude/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:decimalLongitude/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:earliestDateCollected/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:latestDateCollected/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:coordinateUncertaintyInMeters/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:country/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:collector/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:fieldNotes/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:locality/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:maximumDepthInMeters/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:maximumElevationInMeters/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:minimumDepthInMeters/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:minimumElevationInMeters/text()"/>
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="tax:value/text()"/>   
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="../../../../gbif:name/text()"/>   
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="../../gbif:name/text()"/>   
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="../../gbif:rights/text()"/>   
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:text>,</xsl:text>
<xsl:call-template name="wrap-when-comma">
  <xsl:with-param name="unwrappedtext" select="../../gbif:citation/text()"/>   
  <xsl:with-param name="delimiter" select="','"/>
</xsl:call-template>

<xsl:value-of select="$newline"/>   
</xsl:template>

</xsl:stylesheet>
