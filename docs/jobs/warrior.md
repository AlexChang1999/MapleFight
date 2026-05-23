<!-- Context: Part of [Job System Overview](job-system-overview.md) -->
<!-- Domain: #game/jobs -->

- ✅ Job 抽象類 + Warrior（脫戰回血被動）
- ✅ Skill 抽象類 + SkillThrust + SkillShockwave

**修改檔案：** - `entity/Player.java` — 移除 `checkJobUnlock()` 中 `new Warrior()`；改為設定 `jobChangeAvailable = true`；新增 `selectJob(Job)` 方法
