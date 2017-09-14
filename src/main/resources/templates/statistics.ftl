<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="style.css"/>
</head>
<body>
    <table>
        <tr>
            <td style="vertical-align: top;">
                <table>
                    <tr>
                        <td style="vertical-align: top;">
                            MongoDB server time: ${webUtils.formatDateAndTime(mongoServerLocalTime)}
                        </td>
                    </tr>
                    <tr>
                        <form action="/" method="post">
                            <td>Search for word statistics:</td>
                            <td><input type="text" width="100" name="word"/></td>
                            <td><button type="submit">Search</button></td>
                        </form>
                    </tr>
                    <#if requestedWord??>
                        <tr>
                            <td style="vertical-align: top;">
                                Requested word - ${requestedWord}, counter - ${requestedWordCounter}
                            </td>
                        </tr>
                    </#if>
                </table>
            </td>
            <td style="vertical-align: top;">
                <table>
                    <tr><td>Master</td><td>ID</td><td>Start time</td><td>Ping time</td><td>Free mem,%</td><td>It's me</td></tr>
                    <#list listActiveRunners as runner>
                        <tr>
                            <td align="center">
                                <#if runner?index = 0><img src="plus.png" height="22" width="22"/></#if>
                            </td>
                            <td>${runner.runnerUUID}</td>
                            <td>${webUtils.formatDateAndTime(runner.startTimeInMs)}</td>
                            <td>${webUtils.formatDateAndTime(runner.pingTimeInMs)}</td>
                            <td>${runner.freeMemPerc!}</td>
                            <td align="center">
                                <#if runner.runnerUUID = myUUID><img src="smile.png" height="22" width="22"/></#if>
                            </td>
                        </tr>
                    </#list>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <div id="mostCommonWordDiv">
                    <script src="/js/Chart.bundle.js"></script>
                    <canvas id="mostCommonWord" width="650" height="${chartHeightM}"></canvas>
                    <script>
                        var ctx = document.getElementById("mostCommonWord");
                        var myChart = new Chart(ctx, {
                            type: 'horizontalBar',
                            data: {
                                labels: [${wordsM}],
                                datasets: [{
                                    label: '<#if twoReports??>most common words<#else>words</#if>',
                                    data: [${countsM}],
                                    backgroundColor: [${backgroundColorM}],
                                    borderColor: [${borderColorM}],
                                    borderWidth: 1
                                }]
                            },
                            options: {
                                scales: {
                                    xAxes: [{
                                        ticks: {
                                            beginAtZero: true
                                        }
                                    }]
                                }
                            }
                        });
                    </script>
                </div>
            </td>
            <td>
                <#if twoReports??>
                    <div id="leastCommonWordDiv">
                        <script src="/js/Chart.bundle.js"></script>
                        <canvas id="leastCommonWord" width="650" height="${chartHeightL}"></canvas>
                        <script>
                            var ctx = document.getElementById("leastCommonWord");
                            var myChart = new Chart(ctx, {
                                type: 'horizontalBar',
                                data: {
                                    labels: [${wordsL}],
                                    datasets: [{
                                        label: 'least common words',
                                        data: [${countsL}],
                                        backgroundColor: [${backgroundColorL}],
                                        borderColor: [${borderColorL}],
                                        borderWidth: 1
                                    }]
                                },
                                options: {
                                    scales: {
                                        xAxes: [{
                                            ticks: {
                                                beginAtZero: true
                                            }
                                        }]
                                    }
                                }
                            });
                        </script>
                    </div>
                </#if>
            </td>
        </tr>
    </table>
</body>
</html>