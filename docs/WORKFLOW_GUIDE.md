# StoreFlow API - Professional Git Workflow Guide

## Current Status
✅ **Hotfix merged** to main and develop  
✅ **Phase 2 branch** updated with PostgreSQL configuration  
✅ **GitHub Actions CI/CD** configured  
✅ **Docker Compose** ready for local PostgreSQL  

## Your Workflow (Going Forward)

### 1️⃣ **Local Development Setup**

```bash
# Clone if needed
cd "e:\GROOTAN\storeflow api\storeflow-api"

# Start PostgreSQL
docker-compose up -d

# Verify databases ready
docker-compose ps
```

### 2️⃣ **Make Sure You're on Phase 2 Branch**

```bash
git checkout phase-2-data-modeling
git log --oneline -5  # Verify you have hotfix merged
```

### 3️⃣ **Run Tests** 

```bash
# Run all tests
./mvnw clean test -q

# Or with verbose output if needed
./mvnw clean test
```

### 4️⃣ **Fix Test Failures** (if any)

When tests fail:
- Don't create random branches
- Stay on **phase-2-data-modeling**
- Make commits with clear messages
- Example commit pattern:

```bash
git add src/...
git commit -m "fix(repository): fix null reference in CategoryRepositoryTest

- Initialize products list in Category entity
- Verify relationship mapping
- Update test assertions

Fixes: #123"
```

### 5️⃣ **Commit Message Format** (Professional)

Use this pattern:
```
<type>(<scope>): <subject>

<body - what and why>

<footer - references>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `test` - Test-related changes
- `refactor` - Code restructuring
- `chore` - Build, CI/CD, dependencies
- `docs` - Documentation

**Example:**
```bash
git commit -m "feat(entity): add validation to Product entity

- Add @NotEmpty annotation to SKU field
- Add @Min(0) to price field
- Update ProductRepositoryTest with validation tests

This implements: StoreFlow Phase 2 data validation requirements"
```

## Phase 2 Completion Workflow

### When ALL Phase 2 Tests Pass ✅

**Step 1: Create release commit on phase-2**
```bash
git add -A
git commit -m "release(phase-2): Phase 2 complete - data entities and tests passing

Summary:
- ✅ All JPA entities implemented (Product, Category, Store, Order, etc.)
- ✅ 60+ integration tests passing
- ✅ Flyway migrations ready
- ✅ PostgreSQL configuration complete
- ✅ GitHub Actions CI/CD passing

Test Results:
- Tests run: 74
- Failures: 0
- Errors: 0
- Coverage: 85%+"
```

**Step 2: Merge to develop**
```bash
git checkout develop
git merge phase-2-data-modeling -m "merge(phase-2): merge Phase 2 data modeling to develop

- Merged phase-2-data-modeling branch
- 60+ integration tests passing
- All acceptance criteria met
- Ready for Phase 3 feature development"
```

**Step 3: Merge to main**
```bash
git checkout main
git merge develop -m "merge(release): Phase 2 data modeling to production

- Merged develop with Phase 2 complete
- Tagged as v0.2.0
- Ready for production Phase 3 work"

# Tag the release
git tag -a v0.2.0 -m "Phase 2: Data Modeling Complete

Features:
- JPA entity framework
- Repository layer
- Database migrations
- Integration tests"

git log --oneline --graph --all -10  # View history
```

## Git Flow Summary

```
          ┌─────────────────────────┐
          │   hotfix/branch         │
          │  fixes & improvements   │
          └────────┬────────────────┘
                   │
                   ├─→ develop ─→ Phase 2 testing
                   │
                   └─→ main (production)

After Phase 2 completes:
phase-2-data-modeling ─→ develop ─→ main (tag v0.2.0)
```

## Commit Guidelines

✅ **DO:**
- Write clear, concise commit messages
- Reference issues/PRs when applicable
- Group related changes
- Commit frequently (not large monolithic commits)
- Test before committing

❌ **DON'T:**
- Commit broken code
- Mix unrelated changes
- Use vague messages like "fixes" or "updates"
- Skip tests
- Merge without verifying tests pass

## Useful Git Commands

```bash
# See your branch history
git log --oneline -10

# See differences
git diff develop..phase-2-data-modeling

# Check which branch you're on
git branch -v

# Undo last commit (keep changes)
git reset --soft HEAD~1

# View commit details
git show <commit-hash>

# Cherry-pick a commit
git cherry-pick <commit-hash>

# Rebase latest develop
git rebase develop

# View branches with last commit
git branch -v
```

## Quick Reference

| Action | Command |
|--------|---------|
| Start Phase 2 | `git checkout phase-2-data-modeling` |
| Check status | `git status` |
| Add changes | `git add .` |
| Commit | `git commit -m "type: message"` |
| Start tests | `docker-compose up -d && ./mvnw test` |
| View history | `git log --oneline -5` |
| Merge to develop | `git checkout develop && git merge phase-2-data-modeling` |
| Merge to main | `git checkout main && git merge develop` |

## Next Steps

1. ✅ Start PostgreSQL: `docker-compose up -d`
2. ✅ Run tests: `./mvnw clean test`
3. ✅ Fix any failures with commits
4. ✅ When all pass → merge phase-2 → develop → main
5. ✅ Tag as v0.2.0
6. ✅ Start Phase 3

Good luck! 🚀
