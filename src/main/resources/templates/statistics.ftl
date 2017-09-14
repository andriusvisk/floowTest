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
            <td style="vertical-align: top;">
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

                <div id="mostCommonWordDiv">
                    <script src="/js/Chart.bundle.js"></script>
                    <canvas id="mostCommonWord" width="650" height="${chartHeight}"></canvas>
                    <script>
                        var ctx = document.getElementById("mostCommonWord");
                        var myChart = new Chart(ctx, {
                            type: 'horizontalBar',
                            data: {
                                labels: [${mostWords}],
                                datasets: [{
                                    label: 'most common words',
                                    data: [${mostWordCounts}],
                                    backgroundColor: [${backgroundColor}],
                                    borderColor: [${borderColor}],
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
        </tr>
        <tr>
            <td></td>
            <td>
                <div id="leastCommonWordDiv">
                    <script src="/js/Chart.bundle.js"></script>
                    <canvas id="leastCommonWord" width="650" height="${chartHeight}"></canvas>
                    <script>
                        var ctx = document.getElementById("leastCommonWord");
                        var myChart = new Chart(ctx, {
                            type: 'horizontalBar',
                            data: {
                                labels: [${mostWords}],
                                datasets: [{
                                    label: 'least common words',
                                    data: [${mostWordCounts}],
                                    backgroundColor: [${backgroundColor}],
                                    borderColor: [${borderColor}],
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
        </tr>
    </table>
</body>
</html>