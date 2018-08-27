# package-xslt
Transforms the XML to another XML/HTML/plain text using XSL transformations

## how to use?

This is currently depends on ballerina 0.981.0 release. 

 - Go to the project folder and build using "mvn clean install"
 
 - There will be a zip file and a jar file inside the target directory

 - Copy the jar file to BALLERINA_HOME/bre/lib folder
 
 - Extract the zip file and copy the content ("package-xslt-0.981.0-SNAPSHOT-ballerina-binary-repo/repo" folder content)
 to BALLERINA_HOME/lib/repo directory

Then you can use functions in "wso2/xslt" package within your ballerina files.

Sample usage would be as follows (xsltTest.bal)

```
import wso2/xslt;
import ballerina/io;

function main(string... args) {
    xml input = xml `<catalog>
	                       <cd>
	                          <title>Empire Burlesque</title>
	                          <artist>Bob Dylan</artist>
	                          <country>USA</country>
	                          <company>Columbia</company>
	                          <price>10.90</price>
	                          <year>1985</year>
	                       </cd>
                    </catalog>`;
    xml xsl = xml `<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
                        <xsl:template match="/">
                            <html>
                                <body>
                                    <h2>My CD Collection</h2>
                                    <table border="1">
                                        <tr bgcolor="#9acd32">
                                            <th>Title</th>
                                            <th>Artist</th>
                                        </tr>
                                        <xsl:for-each select="catalog/cd">
                                            <tr>
                                                <td>
                                                    <xsl:value-of select="title"/>
                                                </td>
                                                <td>
                                                    <xsl:value-of select="artist"/>
                                                </td>
                                            </tr>
                                        </xsl:for-each>
                                    </table>
                                </body>
                            </html>
                        </xsl:template>
                    </xsl:stylesheet>`;
    
    xml result = xslt:performXSLT(input, xsl);
    
    io:println(result);
}

```

run this with `ballerina run --offline xsltTest.bal`
