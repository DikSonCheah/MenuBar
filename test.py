import os 
from github import Github

g = Github(os.environ.get('token'))
repo = g.get_repo("DikSonCheah/Menubar")
repo.create_issue(title="This is a new issue", body="This is the issue body")
