<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  xmlns:yanel="http://www.wyona.org/yanel/1.0"
  xmlns:i18n="http://www.wyona.org/yanel/i18n/1.0"
>

<!--
  <xsl:output method="xhtml" encoding="UTF-8"/>
-->

  <xsl:param name="yanel.path" select="'PATH_IS_NULL'"/>
  <xsl:param name="yanel.back2realm" select="'BACK2REALM_IS_NULL'"/>
  <xsl:param name="yanel.reservedPrefix" select="'RESERVEDPREFIX_IS_NULL'"/>
  <xsl:param name="content-language" select="'CONTENT_LANGUAGE_IS_NULL'"/>
  <xsl:param name="language" select="'LANGUAGE_IS_NULL'"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Login to realm "<xsl:value-of select="/yanel:yanel-auth-screen/yanel:realm/@name"/>" - Yanel</title>
        <link rel="stylesheet" href="{$yanel.back2realm}{$yanel.reservedPrefix}/yanel-css/global.css" type="text/css"/>
      </head>
      <body>
        <table cellspacing="0" cellpadding="0" id="bodytable">
          <tr>
            <td id="title">
              Login to realm "<xsl:value-of select="/yanel:yanel-auth-screen/yanel:realm/@name"/>"
            </td>
            <td id="logo">  
               <img src="{$yanel.back2realm}{$yanel.reservedPrefix}/yanel-img/yanel_header.png"/>
            </td>
          </tr>
          <tr>
            <td width="100%" valign="top"  colspan="2">
              <div id="content">

        <p>Authorization was denied. Please, enter your username and password.</p>
        <xsl:apply-templates select="/yanel:yanel-auth-screen/yanel:message"/>
          <form method="POST">
            <input type="hidden" name="yanel.login.hash.fragment" id="hash_fragment"/>
<script type="text/javascript">
var hashFragment = window.location.hash.substr(1);
if (hashFragment.length > 0) {
    //alert("DEBUG: Hash fragment: '" + hashFragment + "'");
    document.getElementById('hash_fragment').value = hashFragment;
    //document.write("DEBUG: Hash fragment: " + hashFragment);
}
</script>

            Username:<xsl:choose><xsl:when test="/yanel:yanel-auth-screen/yanel:login-default"><input type="text" name="yanel.login.username" value="{/yanel:yanel-auth-screen/yanel:login-default/@username}"/></xsl:when><xsl:when test="/yanel:yanel-auth-screen/yanel:login-preset"><input type="text" name="yanel.login.username" value="{/yanel:yanel-auth-screen/yanel:login-preset/@username}"/></xsl:when><xsl:otherwise><input type="text" name="yanel.login.username"/></xsl:otherwise></xsl:choose>
            <i18n:text key="password"/>: <input type="password" name="yanel.login.password"/>(<a id="forgotpw" href="{$yanel.back2realm}{$yanel.reservedPrefix}/user-forgot-pw.html"><i18n:text key="forgot.password"/></a>)<br/>
            <xsl:choose><xsl:when test="/yanel:yanel-auth-screen/yanel:login-default"><input type="checkbox" name="remember-my-login-name" checked="checked"/></xsl:when><xsl:otherwise><input type="checkbox" name="remember-my-login-name"/></xsl:otherwise></xsl:choose> Remember my login name (for 1 day (<a href="">More info</a>). Uncheck if on a shared computer!)
            <br/><input type="checkbox" name="auto-login"/> Keep me signed in (<a href="?yanel.usecase=logout">Logout</a> explicitely in order to remove auto login cookie and hence disable auto login)
            <input type="submit" value="Login" name="regular-login"/>
          </form>
        <xsl:apply-templates select="/yanel:yanel-auth-screen/yanel:ssl"/>
	<p>
	or login with <a href="http://www.openid.net">OpenID</a>:
	<form method="POST">
          <xsl:choose><xsl:when test="/yanel:yanel-auth-screen/yanel:login-openid"><input type="text" name="yanel.login.openid" value="{/yanel:yanel-auth-screen/yanel:login-openid/@openid}"/></xsl:when><xsl:otherwise><input type="text" name="yanel.login.openid"/></xsl:otherwise></xsl:choose>
          <xsl:choose><xsl:when test="/yanel:yanel-auth-screen/yanel:login-openid"><input type="checkbox" name="remember-my-login-name" checked="checked"/></xsl:when><xsl:otherwise><input type="checkbox" name="remember-my-login-name"/></xsl:otherwise></xsl:choose> Remember my OpenID (for 1 day (<a href="">More info</a>). Uncheck if on a shared computer!)
          <input type="submit" value="Login" name="openid-login"/>
	</form>
	(For example: username.wordpress.com)
	</p>
        <hr/>
        <p>
        <xsl:choose>
          <xsl:when test="/yanel:yanel-auth-screen/yanel:request/@qs">
            <a href="?{/yanel:yanel-auth-screen/yanel:request/@qs}&amp;yanel.resource.viewid=xml">Show XML source</a>
          </xsl:when>
          <xsl:otherwise>
            <a href="?&amp;yanel.login.format=xml">Show XML source</a>
          </xsl:otherwise>
        </xsl:choose>
        </p>
        <xsl:apply-templates select="/yanel:yanel-auth-screen/yanel:request"/>
        <xsl:apply-templates select="/yanel:yanel-auth-screen/yanel:realm"/>
        <xsl:apply-templates select="/yanel:yanel-auth-screen/yanel:user"/>
        </div>
        </td>
</tr>
</table>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="yanel:message">
      <div id="message"><xsl:value-of select="."/></div>
  </xsl:template>

  <xsl:template match="yanel:request">
    Request: <xsl:value-of select="@urlqs"/>
    <br/>
    Path starting at realm: <xsl:value-of select="$yanel.path"/>
    <br/>
    Content language: <xsl:value-of select="$content-language"/>
    <br/>
    User language: <xsl:value-of select="$language"/>
  </xsl:template>

  <xsl:template match="yanel:ssl">
      <xsl:choose>
        <xsl:when test="contains(@status, 'ON')">
          <p><img src="{$yanel.back2realm}{$yanel.reservedPrefix}/yanel-img/ssl_on.png" alt="ssl on"/> SSL support is ON</p>
        </xsl:when>
        <xsl:otherwise>
          <h4><img src="{$yanel.back2realm}{$yanel.reservedPrefix}/yanel-img/ssl_off.png" alt="ssl off"/> Warning: SSL support is OFF. Your username and password will be sent as plain text.</h4>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

  <xsl:template match="yanel:realm">
    <p>
    Realm info:
       <ul>
        <li>Name: <xsl:value-of select="@name"/></li>
        <li>Mount Point: <xsl:value-of select="@mount-point"/></li>
       </ul>
    </p>
  </xsl:template>

  <xsl:template match="yanel:user">
    <p>
    Currently signed in into this realm as:
       <ul>
        <li>User ID: <xsl:value-of select="@id"/></li>
       </ul>
    </p>
  </xsl:template>

</xsl:stylesheet>
