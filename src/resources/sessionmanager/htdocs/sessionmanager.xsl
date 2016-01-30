<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:s="http://www.wyona.org/yanel/1.0"
  xmlns="http://www.w3.org/1999/xhtml">

  <xsl:template match="/s:session-manager">
    <html>
      <head>
        <title>Session manager</title>
      </head>
      <body>
        <h1>Session manager</h1>
        <strong>Total number of sessions: </strong>
        <xsl:value-of select="s:number-of-sessions"/>

        <h2>All "logged-in" (or previously logged-in) sessions</h2>
        <!-- INFO: Show all logged-in sessions first -->
        <xsl:if test="not(s:session[s:identities])">
          <p>No "logged-in" sessions yet.</p>
        </xsl:if>
        <ol>
          <xsl:for-each select="s:session[s:identities]">
            <xsl:sort select="@last-accessed-time" order="descending"/>
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </ol>

        <h2>All anonymous sessions</h2>
        <!-- INFO: Show all anonymous sessions later -->
        <xsl:if test="not(s:session[s:no-identity-yet])">
          <p>No "anonymous" sessions yet.</p>
        </xsl:if>
        <ol>
          <xsl:apply-templates select="s:session[s:no-identity-yet]"/>
        </ol>

        <hr/>
        <p><a href="?yanel.resource.viewid=source">XML</a> (Please make sure to configure a view called "source" within your resource configuration, e.g. see <em>global-resource-configs/session-manager_yanel-rc.xml</em>)</p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="s:session">
    <li>
      <strong>Session: </strong>
      <ul>
        <li>
          <strong>Hashed ID: </strong>
          <xsl:value-of select="@id"/>
        </li>
        <li>
          <strong>Creation time: </strong>
          <xsl:value-of select="@creation-time"/>
        </li>
        <li>
          <strong>Last accessed time: </strong>
          <xsl:value-of select="@last-accessed-time"/>
        </li>
        <xsl:if test="s:identities">
          <li>
            <strong>Associated identities (username and realm): </strong>
            <xsl:value-of select="s:identities"/>
          </li>
        </xsl:if>
        <xsl:if test="s:cas-proxy-ticket">
          <li>
            <strong>Hashed CAS proxy ticket: </strong>
            <xsl:value-of select="s:cas-proxy-ticket"/>
          </li>
        </xsl:if>
        <xsl:if test="s:cas-ticket">
          <li>
            <strong>Hashed CAS ticket: </strong>
            <xsl:value-of select="s:cas-ticket"/>
          </li>
        </xsl:if>

        <xsl:if test="s:last-accessed-url">
          <li>
            <strong>Last accessed URL: </strong>
            <xsl:value-of select="s:last-accessed-url"/>
          </li>
        </xsl:if>
      </ul>
      <br/>
    </li>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
