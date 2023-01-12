# Detector search troubleshooting details

The [Detectors](detectors.md) page contains a table that provides relevant information regarding each Detector type,
entry point, and detector.  This page contains a similar table, but adds additional details not present on the Detectors page.
This page is intended for troubleshooting, which is when these additional details may be relevant and useful.

Details on detector search is available on the [detector search](../downloadingandrunning/detectorcascade.md) page.

|Detector Type|Entry Point|Detector|Language|Forge|Requirements|Accuracy|
|---|---|---|---|---|---|---|
<#list detectorTypes as detectorType>
|**${detectorType.name}**|||||||
<#list detectorType.entryPoints as entryPoint>
||${entryPoint.name}||||||
<#list entryPoint.detectables as detectable>
||| ${detectable.name} |${detectable.language!""}|${detectable.forge!""} | <#if detectable.requirementsMarkdown?has_content ><#noautoesc>${detectable.requirementsMarkdown!""}</#noautoesc></#if>|${detectable.accuracy!""}|
</#list>
</#list>
</#list>


