<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:i18n="http://www.wyona.org/yanel/i18n/1.0"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:yanel="http://www.wyona.org/yanel/1.0"
>

<!-- DEPRECATED -->

  <xsl:output method="xhtml" encoding="UTF-8"/>
  
  <xsl:param name="yanel.back2context" select="''" />
  
  <xsl:param name="realmid" select="''" />
  <xsl:param name="realmname" select="''" />
  <xsl:param name="url" select="''" />
  <xsl:param name="scope" select="''" />
  <xsl:param name="fslocation" select="''" />
  <xsl:param name="crawldepth" select="''" />
  <xsl:param name="crawlmaxpages" select="''" />
  <xsl:param name="crawlmaxfilesize" select="''" />
  
  <xsl:param name="downloadevents" select="''" />
  <xsl:param name="errorevents" select="''" />
  <xsl:param name="nofdownloads" select="''" />
  <xsl:param name="isdone" select="''" />

  <xsl:param name="submitted" select="'false'" />
  
  
  <xsl:template match="yanel:form">
    <table border="0">
      <xsl:apply-templates/>
    </table>
  </xsl:template>
  
  
  <xsl:template match="yanel:inputfields">
    <xsl:apply-templates select="@*|node()"/>
  </xsl:template>
  
  
  <xsl:template match="yanel:input">
    <div>
      <xsl:if test="@yanel:hidden = 'true'">
        <xsl:attribute name="style">display:none;</xsl:attribute>
      </xsl:if>
      <tr>
        <td width="150" rowspan="2"><i18n:message key="{@yanel:name}"/>:&#160;</td>

        <xsl:choose>
        <xsl:when test="@yanel:value">
          <td colspan="2"><strong><xsl:value-of select="@yanel:value"/></strong></td>
        </xsl:when>
        <xsl:otherwise>
        <td align="left" width="175">
          <input name="{@yanel:name}" size="25">
            <xsl:choose>
              <xsl:when test="@yanel:value">
                <xsl:attribute name="type">hidden</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="type">text</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
          </input>
        </td>
        <td>&#160;<xsl:if test="@yanel:required='true'">*</xsl:if></td>
      </xsl:otherwise>
      </xsl:choose>
      </tr>
      <xsl:if test="not(@yanel:value)">
        <tr><td class="samplevalue" colspan="2">(i.e. <xsl:value-of select="@yanel:samplevalue"/>)</td></tr>
      </xsl:if>
    </div>
  </xsl:template>
  

  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <title><i18n:message key="add-realm"/></title>
        <style>.samplevalue {padding-bottom:10px;}</style>
        <xsl:if test="$isdone = 'false'">
          <meta http-equiv="refresh" content="2"/>
        </xsl:if>
      </head>
      <body>
        <h3><i18n:message key="add-realm"/></h3>
        
        <div id="contentBody">
          <xsl:choose>
            <xsl:when test="$submitted != 'false'">
              <xsl:choose>
                <xsl:when test="$isdone = 'true'">
                  <h2>Import completed</h2>
                  <p>
                    <a>
                      <xsl:attribute name="href">
                        <xsl:value-of select="$yanel.back2context"/><xsl:value-of select="$realmid"/>/
                      </xsl:attribute>View <xsl:value-of select="$realmname" />
                    </a>
                  </p>
                </xsl:when>
                <xsl:otherwise>
                  <h2>Import running, please wait...</h2>
                  <form>
                    <input type="submit" value="Stop Crawl" name="stop"/>
                  </form>
                </xsl:otherwise>
              </xsl:choose>
              <p>[<xsl:value-of select="$nofdownloads"/>] pages have been imported.</p>
              <p>Downloaded Pages:</p>
              <p style="font-size: small"><pre><xsl:value-of select="$downloadevents"/></pre></p>
              <p>Errors:</p>
              <p style="font-size: small"><pre><xsl:value-of select="$errorevents"/></pre></p>
            </xsl:when>
            <xsl:otherwise>
              <form method="post">
                <table cellpadding="0" cellspacing="0" border="0">
                  <xsl:apply-templates select="yanel:form"/>
                  <tr><td>&#160;</td></tr>
                  <tr>
                    <td align="right">
                      <input type="submit" name="submit" value="i18n:attr key=add-realm"/>
                    </td>
                  </tr>
                  <tr>
                    <td align="right" valign="top" class="contentfield" colspan="2"><br/><i18n:message key="requiredFields"/></td>
                  </tr>
                </table>
              </form>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </body>
    </html>
  </xsl:template>
   
</xsl:stylesheet>
