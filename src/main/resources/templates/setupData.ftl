<#setting number_format=",##0.00000000">
<#assign total=0>
<!DOCTYPE html>

<html lang="en">

<body>
	<table border="1">
	<#list setupData?keys as key>
		<tbody>
        <tr>
            <td>${key}</td>
            <td>${setupData[key]} </td>
        </tr>
        </tbody>
    </#list>
    </table>
</body>

</html>