from github import Github
from datetime import datetime
import os

team = ["DikSonCheah", "kokwei217"]


g = Github(os.environ.get('token'))
repo = g.get_repo(os.environ.get('repo'))
branches = repo.get_branches()

for branch in branches:
    commit = repo.get_commit(branch.name)
    last_commitDate = commit.commit.author.date
    commit_author = commit.commit.author.name

    duration = datetime.now() - last_commitDate
    days = duration.days

    print(branch.name)
    print("date: " + str(last_commitDate))
    print("author: " + commit_author)
    print(days)

    if days >= 90:
        if commit_author in team:
            repo.create_issue(title=f"Stale branch called {branch.name}", body=f"{branch.name} branch last commited by @{commit_author} at {last_commitDate}",
                              assignee=commit_author)

        elif commit_author not in team:
            repo.create_issue(title=f"Stale branch called {branch.name}", body=f"{branch.name} branch last commited by {commit_author} at {last_commitDate}",
                              assignees=team)
