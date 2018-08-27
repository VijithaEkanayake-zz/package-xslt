documentation {
        Transforms the XML to another XML/HTML/plain text using XSL transformations.

        P{{input}} An XML object which needs to be transformed.
        P{{xsl}} The XSL style sheet as an XML object.
        R{{}} The transformed result as an XML object.
    }
public extern function performXSLT(xml input, xml xsl) returns xml;