<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Remove JSPWiki content</title>
</head>
<script>
function confirmation()
{
	return confirm("Do you really want to remove this Wiki content ?");
}	
</script>
<body>

<H3>
Important: this operation is not recoverable !<br>
You are about to remove the whole Wiki content !<br><br>
Make sure that you understand what you are going to do.
</H3>

<a href="/">Return to JSP Wiki without removing anything (recommended) !!!</a>

<br />
<br>
<div style=" border-style:solid; border-width:1px; text-align:left; width:30%;">
<table>
<tr>
<td>
Are you sure that you want to remove JSP Wiki content ?
</td>
</tr>
<tr>
<td>
<form name="removefrm" action="RemoveJSPWikiNotWise" onsubmit="return confirmation()">
<input type="submit" value="Remove Wiki content" />
</td>
</tr>
</table>
</div>


</body>
</html>