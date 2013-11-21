<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <%@ page import="java.io.FileInputStream" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>BioSTIF Fileuploader to Geoserver</title>

<script type="text/javascript">

function checkBox1(){
	document.getElementById("radio1").checked=true;
}
function checkBox2(){
	document.getElementById("radio2").checked=true;
}

function clear(){
	document.getElementById("input1").value="";
	document.getElementById("input2").value="";
	document.getElementById("input3").value="";
	document.getElementById("input4").value="";
	//document.getElementById("radio1").checked=false;
	//document.getElementById("radio2").checked=false;
}

function clear1(){
	document.getElementById("input1").value="";
	document.getElementById("input2").value="";
	document.getElementById("input3").value="";
	document.getElementById("input4").value="";
}

function write_path(filename){
	document.getElementById("input4").value=filename;
}

function write_pathCSV(filename){
	document.getElementById("input5").value=filename;
}

function write_pathCSV1(filename){
	document.getElementById("input6").value=filename;
}


</script>

</head>
<body onload="clear()">

<!-- 

---- DwC2CSV -------
</br>Input Species Name:
<form name="form1" action="<%=request.getContextPath() %>/Dwc2Csv" method="post">
<input type="text" id="input1" name="scientificname" onclick="clear1()"> 
</br>
<input type="submit">
</form>
</br></br>
---- DwC2Json -------
</br>Input Species Name:
<form name="form2" action="<%=request.getContextPath() %>/Dwc2Json" method="post">
<input type="text" name="scientificname" id="input2" onclick="clear1()">
<input type="submit">
</form>

<form name="form3" action="<%=request.getContextPath() %>/Dwc2Json" enctype="multipart/form-data" method="post">
------------ </br>Input File Location:
</br><input type="file" name="file" id="input3" onclick="clear1()" onChange="write_path(this.value)" size=20>
</br> Filename: </br><input type="text" name="fileName" id="input4" size=40> 
</br><input type="submit" value="Upload">
</form>

</br></br>
---- FilterDwC -------
<form name="form4" action="<%=request.getContextPath() %>/Csv2Json" enctype="multipart/form-data" method="post">
Input File Location:
</br><input type="file" name="file" id="input4" onclick="clear1()" onChange="write_pathCSV(this.value)" size=20>
</br> Filename: </br><input type="text" name="fileName" id="input5" size=40> 
</br><input type="submit" value="Upload">
</form>

-->

</br>
<img alt="BioVeL Logo" src="img/stiflogo.jpg" align="bottom"/></a>
<br><br>
Fileupload helper to BioSTIF / Geoserver <br><br>
currently supported file formats:<br> ERDAS Imagine (*.img), GeoTIFF (*.tiff) and ArcGrid (*.arcgrid)<br><br>
choose your file and press the "upload" button<br><br>
<form name="form5" action="<%=request.getContextPath() %>/Imageupload" enctype="multipart/form-data" method="post">
Input File Location:
</br><input type="file" name="file" id="input5" onclick="clear1()" onChange="write_pathCSV1(this.value)" size=20>
</br> filename: </br><input type="text" name="fileName" id="input6" size=40 disabled="disabled"> 
</br><input type="submit" value="Upload">
</form>



<!-- 
</br>Input Species Name:
<form name="form2" action="<%=request.getContextPath() %>/Dwc2Json" method="post">
<INPUT TYPE="radio" NAME="radios" VALUE="radio1" id="radio1">
<input type="text" name="scientificname" id="input2" onclick="checkBox1()">
</br>Input File Location:
</br><INPUT TYPE="radio" NAME="radios" VALUE="radio2" id="radio2">
<input type="file" name="fileURL" id="input3" onChange="write_path(this.value)"  onclick="checkBox2()"> 
<input type="text" name="path" size="100">
</br>
<input type="submit">
</form>
-->




</body>
</html>