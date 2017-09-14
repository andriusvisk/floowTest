<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="style.css"/>
</head>
<body>
    <p class="styleclass">MongoDB server time: ${webUtils.formatDateAndTime(mongoServerLocalTime)}</p>
    <table>
        <tr>
            <td>
                <table>
                    <tr><td>Master</td><td>ID</td><td>Start time</td><td>Ping time</td><td>It's me</td></tr>
                    <#list listActiveRunners as runner>
                        <tr>
                            <td align="center">
                                <#if runner?index = 0><img src="plus.png" height="22" width="22"/></#if>
                            </td>
                            <td>${runner.runnerUUID}</td>
                            <td>${webUtils.formatDateAndTime(runner.startTimeInMs)}</td>
                            <td>${webUtils.formatDateAndTime(runner.pingTimeInMs)}</td>
                            <td align="center">
                                <#if runner.runnerUUID = myUUID><img src="smile.png" height="22" width="22"/></#if>
                            </td>
                        </tr>
                    </#list>
                </table>
            </td>
            <td>


            </td>
        </tr>
    </table>
</body>
</html>