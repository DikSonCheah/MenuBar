import subprocess

p = subprocess.Popen("git log", shell=True, stdout=subprocess.PIPE, universal_newlines=True)
print(p.communicate()[0])
