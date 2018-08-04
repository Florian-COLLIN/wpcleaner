/*
 *  WPCleaner: A tool to help on Wikipedia maintenance tasks.
 *  Copyright (C) 2013  Nicolas Vervelle
 *
 *  See README.txt file for licensing information.
 */

package org.wikipediacleaner.api.request.query.list;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.wikipediacleaner.api.APIException;
import org.wikipediacleaner.api.constants.EnumWikipedia;
import org.wikipediacleaner.api.data.AbuseFilter;
import org.wikipediacleaner.api.request.ApiRequest;
import org.wikipediacleaner.api.request.ApiXmlResult;


/**
 * MediaWiki API XML abuse filters results.
 */
public class ApiXmlAbuseFiltersResult extends ApiXmlResult implements ApiAbuseFiltersResult {

  /**
   * @param wiki Wiki on which requests are made.
   * @param httpClient HTTP client for making requests.
   */
  public ApiXmlAbuseFiltersResult(
      EnumWikipedia wiki,
      HttpClient httpClient) {
    super(wiki, httpClient);
  }

  /**
   * Execute abuse filters request.
   * 
   * @param properties Properties defining request.
   * @param list List to be filled with abuse filters.
   * @return True if request should be continued.
   * @throws APIException Exception thrown by the API.
   */
  @Override
  public boolean executeAbuseFilters(
      Map<String, String> properties,
      List<AbuseFilter> list) throws APIException {
    try {
      Element root = getRoot(properties, ApiRequest.MAX_ATTEMPTS);

      // Retrieve category members
      XPathExpression<Element> xpa = XPathFactory.instance().compile(
          "/api/query/abusefilters/filter", Filters.element());
      List<Element> results = xpa.evaluate(root);
      Iterator<Element> iter = results.iterator();
      while (iter.hasNext()) {
        Element currentNode = iter.next();
        Integer id = Integer.valueOf(0);
        try {
          String tmp = currentNode.getAttributeValue("id");
          if (tmp != null) {
            id = Integer.parseInt(tmp);
          }
        } catch (NumberFormatException e) {
          //
        }
        String description = currentNode.getAttributeValue("description");
        AbuseFilter filter = new AbuseFilter(id, description);
        filter.setDeleted(currentNode.getAttribute("deleted") != null);
        filter.setEnabled(currentNode.getAttribute("enabled") != null);
        list.add(filter);
      }

      // Retrieve continue
      return shouldContinue(
          root, "/api/query-continue/abusefilters",
          properties);
    } catch (JDOMException e) {
      log.error("Error loading abuse filters list", e);
      throw new APIException("Error parsing XML", e);
    }
  }
}
