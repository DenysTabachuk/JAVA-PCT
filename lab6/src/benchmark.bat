@echo off
setlocal enabledelayedexpansion

set procs=2 4 6 8 12 16

for %%p in (%procs%) do (
 mpjrun.bat -np %%p Main -noVerbose %%s
)