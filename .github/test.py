from github import Github
from datetime import datetime
import os
import time
import re

active_team = {,
               }

stale_branch_issues = []
infos = {}


g = Github(os.environ.get('token'))
repo = g.get_repo(os.environ.get('repo'))
branches = repo.get_branches()
issues = repo.get_issues()


# obtain all issues with stale branch keyword
for issue in issues:
    if 'Stale branch' in issue.title:
        stale_branch_issues.append(issue)

for branch in branches:

    commit = repo.get_commit(branch.commit.sha)
    last_commitDate = commit.commit.author.date
    committer_email = commit.commit.author.email

    duration = datetime.now() - last_commitDate
    days = duration.days

    if days >= 0:

        if 'github' in committer_email:
            committer_email = re.search(r"(?<=\+)\w*", committer_email).group()

        elif committer_email in active_team:
            committer_email = active_team[committer_email]

        if stale_branch_issues:

            for issue in stale_branch_issues:
                if committer_email in issue.title:

                    if committer_email in infos:

                        infos[committer_email][0].append(branch.name)
                        infos[committer_email][1].append(str(days))

                    elif committer_email not in infos:
                        infos[committer_email] = [[branch.name], [str(days)], issue.title, issue.number]
        else:

            if committer_email in infos:

                infos[committer_email][0].append(branch.name)
                infos[committer_email][1].append(str(days))

            elif committer_email not in infos:
                infos[committer_email] = [[branch.name], [str(days)], None, None]


for info in infos:
    if infos[info][2] is None:

        if info in active_team.values():

            repo.create_issue(title="Stale branch/branches detected last commited by {} assigned to {}".format(info, info),
                              body="These branch/branches {} commited from {} days ago respectively".format(", ".join(infos[info][0]), ", ".join(infos[info][1])))
            time.sleep(1.5)

        elif info not in active_team.values():

            repo.create_issue(title="Stale branch/branches detected last commited by {} assigned to2 {}".format(info, info),
                              body="These branch/branches {} commited from {} days ago respectively".format(", ".join(infos[info][0]), ", ".join(infos[info][1])))
            time.sleep(1.5)

    elif infos[info][2] is not None:
        issue_id = repo.get_issue(number=infos[info][3])
        issue_id.create_comment("These branch/branches {} are still stale".format(", ".join(infos[info][0])))
        time.sleep(1.5)

