/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.request.expandtemplates;

import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.request.ApiRequest;
import org.wikipediacleaner.api.request.ApiXmlResult;


/**
 * MediaWiki API XML expand results.
 */
public class ApiXmlExpandResult extends ApiXmlResult implements ApiExpandResult {

  /**
   * @param wiki Wiki on which requests are made.
   * @param httpClient HTTP client for making requests.
   */
  public ApiXmlExpandResult(
      EnumWikipedia wiki,
      HttpClient httpClient) {
    super(wiki, httpClient);
  }

  /**
   * Execute expand templates request.
   * 
   * @param properties Properties defining request.
   * @return Expanded text.
   * @throws APIException Exception thrown by the API.
   */
  @Override
  public String executeExpandTemplates(
      Map<String, String> properties)
          throws APIException {
    try {
      XPathExpression<Element> xpaText = XPathFactory.instance().compile(
          "/api/expandtemplates/wikitext", Filters.element());
      Element root = getRoot(properties, ApiRequest.MAX_ATTEMPTS);
      Element text = xpaText.evaluateFirst(root);
      return (text != null) ? text.getText() : null;
    } catch (JDOMException e) {
      log.error("Error expanding templates", e);
      throw new APIException("Error parsing XML", e);
    }
  }
}
