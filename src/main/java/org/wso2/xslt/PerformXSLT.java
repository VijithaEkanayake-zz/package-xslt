/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.xslt;

import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axiom.om.util.AXIOMUtil;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BRefValueArray;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.model.values.BXMLItem;
import org.ballerinalang.model.values.BXMLSequence;
import org.ballerinalang.nativeimpl.lang.utils.ErrorHandler;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Transforms XML to another XML/HTML/plain text using XSLT.
 */
@BallerinaFunction(
        orgName = "wso2", packageName = "xslt:0.0.0",
        functionName = "performXSLT",
        args = {@Argument(name = "input", type = TypeKind.XML),
                @Argument(name = "xsl", type = TypeKind.XML)},
        returnType = {@ReturnType(type = TypeKind.XML)},
        isPublic = true
)
public class PerformXSLT extends BlockingNativeCallableUnit {

    private static final String OPERATION = "perform XSL transformation";

    @Override
    public void execute(Context ctx) {
        BValue result = null;
        try {
            // Accessing Parameters.
            String input = ((BXML) ctx.getRefArgument(0)).toString();
            String xsl = ((BXML) ctx.getRefArgument(1)).toString();
            OMElement omXML = AXIOMUtil.stringToOM(input);
            OMElement omXSL = AXIOMUtil.stringToOM(xsl);

            StAXSource xmlSource = new StAXSource(omXML.getXMLStreamReader());
            StAXSource xslSource = new StAXSource(omXSL.getXMLStreamReader());

            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);

            // The XML transformation.
            Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.transform(xmlSource, streamResult);

            // Get XSLT result as a string.
            String resultStr = stringWriter.getBuffer().toString().trim();

            if (resultStr.isEmpty()) {
                ctx.setReturnValues();
            } else {
                result = parseToBXML(resultStr);
                // Setting output value.
                ctx.setReturnValues(result);
            }

        } catch (ClassCastException e) {
            ErrorHandler.handleXMLException(OPERATION, new BallerinaException("invalid inputs(s)"));
        } catch (Throwable e) {
            ErrorHandler.handleXMLException(OPERATION, e);
        }
    }

    /**
     * Converts the given string to a BXMLSequence object.
     *
     * @param xmlStr The string to be converted
     * @return The result BXMLSequence object
     * @throws XMLStreamException When converting `xmlStr` to an Axiom OMElement
     */
    @SuppressWarnings("unchecked")
    private BXMLSequence parseToBXML(String xmlStr) throws XMLStreamException {
        // Here we add a dummy enclosing tag, and send it to AXIOM to parse the XML.
        // This is to overcome the issue of AXIOM not allowing to parse XML-comments,
        // XML-text nodes, and PI nodes, without having a XML-element node.
        OMElement omElement = AXIOMUtil.stringToOM("<root>" + xmlStr + "</root>");
        Iterator<OMNode> children = omElement.getChildren();

        // Here we go through the iterator and add all the children nodes to a BRefValueArray.
        // The BRefValueArray is used to create a BXMLSequence object.
        BRefValueArray omNodeArray = new BRefValueArray();
        OMDocument omDocument;
        OMNode omNode;
        int omNodeIndex = 0;
        while (children.hasNext()) {
            omNode = children.next();
            // Here the OMNode is detached from the dummy root, and added to a document element.
            // This is to get the XPath working correctly.
            children.remove();
            omDocument = new OMDocumentImpl();
            omDocument.addChild(omNode);
            omNodeArray.add(omNodeIndex, new BXMLItem(omNode));
            omNodeIndex++;
        }

        return new BXMLSequence(omNodeArray);
    }
}
