---
name: harambe
description: Orchestrate Project Tarbook tasks by decomposing developer intent and delegating across the 10 specialized agent personas.
---

# /harambe — Chief Orchestrator Flow

Use `/harambe <task>` to triage, plan, and delegate complex tasks across Project Tarbook's specialized agents.

## Flow Execution

When `/harambe` is triggered:

1. **Classify Intent**:
   Map the task to the necessary specialist agents:
   - Domain rules / STCW / workflows → [`domain-requirements-analyst`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/domain-requirements-analyst.md)
   - Architecture / bounded contexts / seams → [`architecture-analyst`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/architecture-analyst.md)
   - APIs / backend logic / services → [`backend-engineer`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/backend-engineer.md)
   - Database / migrations / schemas → [`persistence-engineer`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/persistence-engineer.md)
   - Auth / integrity / tamper evidence → [`security-integrity-analyst`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/security-integrity-analyst.md)
   - Offline / sync queues / conflicts → [`offline-sync-analyst`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/offline-sync-analyst.md)
   - Mobile UI / camera / scan evidence → [`mobile-evidence-engineer`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/mobile-evidence-engineer.md)
   - Docker / Compose / env config → [`infrastructure-engineer`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/infrastructure-engineer.md)
   - CI/CD / GitHub Actions / release → [`ci-cd-engineer`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/ci-cd-engineer.md)
   - Tests / quality / verification → [`verification-quality-engineer`](file:///c:/Users/Neeraj%20Gupta/Projects/mralmostcool/tarbook-project/.agents/agents/verification-quality-engineer.md)

2. **Order Execution Plan**:
   Sequence the steps adhering to the project engineering progression:
   `Understand → Model → Decide → Implement → Verify`.

3. **Delegate & Execute**:
   - For single-domain tasks: adopt the specialist agent's instructions directly.
   - For multi-phase tasks: execute phase-by-phase or spawn subagents for parallel/isolated work.

4. **Verify & Synthesize**:
   Run verification with Persona J test criteria before reporting final results to the developer.
