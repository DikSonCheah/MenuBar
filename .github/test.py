from github import Github
from datetime import datetime
import os

team = ["DikSonCheah", "kokwei217"]
stale_branch_issues = []
branch_infos = {}


def main():

    g = Github(os.environ.get('token'))
    repo = g.get_repo(os.environ.get('repo'))
    branches = repo.get_branches()
    issues = repo.get_issues()

    for issue in issues:
        if 'Stale branch' in issue.title:
            stale_branch_issues.append(issue)

    for branch in branches:
        commit = repo.get_commit(branch.name)
        last_commitDate = commit.commit.author.date
        commit_author = commit.commit.author.name

        duration = datetime.now() - last_commitDate
        days = duration.days

        if stale_branch_issues:
            for issue in stale_branch_issues:
                if branch.name in issue.title:
                    branch_infos[branch.name] = [commit_author, last_commitDate, days, issue.title, issue.number]
                    break
                else:
                    branch_infos[branch.name] = [commit_author, last_commitDate, days, None, None]
        else:
            branch_infos[branch.name] = [commit_author, last_commitDate, days, None, None]

    for branch_info in branch_infos:
        if branch_infos[branch_info][3] is None:

            if branch_infos[branch_info][2] >= 1:

                if branch_infos[branch_info][0] in team:
                    repo.create_issue(title=f"Stale branch detected, {branch_info}", body=f"{branch_info} last commited by {branch_infos[branch_info][0]} at {branch_infos[branch_info][1]}"
                                      )

                elif branch_infos[branch_info][0] not in team:
                    repo.create_issue(title=f"Stale branch detected, {branch_info}", body=f"{branch_info} last commited by {branch_infos[branch_info][0]} at {branch_infos[branch_info][1]}"
                                      )

        elif branch_infos[branch_info][3] is not None:
            issue_id = repo.get_issue(number=branch_infos[branch_info][4])
            issue_id.create_comment("This branch is still stale")


if __name__ == "__main__":
    main()
