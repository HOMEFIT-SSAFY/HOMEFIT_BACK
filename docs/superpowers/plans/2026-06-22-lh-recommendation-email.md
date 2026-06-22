# LH Recommendation Email Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build LH rental favorite storage, personalized recommendations, and scheduled email reminders.

**Architecture:** Add focused rental subpackages for favorites, recommendations, and email reminders. Persist favorites and email logs with MyBatis, keep recommendation scoring deterministic, and use Spring Mail behind a small sender service.

**Tech Stack:** Spring Boot 4, Spring MVC, Spring Security, MyBatis, JavaMailSender, JUnit 5, Mockito.

---

### Task 1: Persistence And DTOs

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Modify: `src/main/resources/schema.sql`
- Create: `src/main/java/com/happyhome/rental/favorite/dao/FavoriteRentalNoticeMapper.java`
- Create: `src/main/java/com/happyhome/rental/favorite/dao/FavoriteRentalNoticeDao.java`
- Create: `src/main/resources/mappers/FavoriteRentalNoticeMapper.xml`
- Create: `src/main/java/com/happyhome/rental/email/dao/RentalNoticeEmailLogMapper.java`
- Create: `src/main/java/com/happyhome/rental/email/dao/RentalNoticeEmailLogDao.java`
- Create: `src/main/resources/mappers/RentalNoticeEmailLogMapper.xml`

- [ ] Write failing mapper/service-level tests for favorite save/delete/list and email log duplicate checks.
- [ ] Add mail dependency and SMTP properties.
- [ ] Add schema tables and MyBatis mappers.
- [ ] Run targeted tests.

### Task 2: Favorite APIs

**Files:**
- Create: `src/main/java/com/happyhome/rental/favorite/service/FavoriteRentalNoticeService.java`
- Create: `src/main/java/com/happyhome/rental/favorite/controller/FavoriteRentalNoticeRestController.java`
- Test: `src/test/java/com/happyhome/rental/favorite/service/FavoriteRentalNoticeServiceTest.java`

- [ ] Write failing tests for toggle and favorite listing.
- [ ] Implement service using existing `RentalService.detail`.
- [ ] Add authenticated REST endpoints.
- [ ] Run targeted tests.

### Task 3: Recommendation Engine

**Files:**
- Create: `src/main/java/com/happyhome/rental/recommendation/dto/RentalRecommendation.java`
- Create: `src/main/java/com/happyhome/rental/recommendation/service/RentalRecommendationService.java`
- Create: `src/main/java/com/happyhome/rental/recommendation/controller/RentalRecommendationRestController.java`
- Test: `src/test/java/com/happyhome/rental/recommendation/service/RentalRecommendationServiceTest.java`

- [ ] Write failing scoring tests for affordable active notices ranking above unaffordable/closed notices.
- [ ] Implement scoring from financial profile, status, dates, and expected amount text.
- [ ] Add authenticated recommendation endpoint.
- [ ] Run targeted tests.

### Task 4: Email Reminder Engine

**Files:**
- Create: `src/main/java/com/happyhome/rental/email/dto/RentalNoticeEmailEventType.java`
- Create: `src/main/java/com/happyhome/rental/email/service/RentalNoticeDateParser.java`
- Create: `src/main/java/com/happyhome/rental/email/service/RentalNoticeEmailService.java`
- Create: `src/main/java/com/happyhome/rental/email/service/RentalNoticeEmailScheduler.java`
- Create: `src/main/java/com/happyhome/rental/email/controller/RentalNoticeEmailRestController.java`
- Test: `src/test/java/com/happyhome/rental/email/service/RentalNoticeEmailServiceTest.java`

- [ ] Write failing tests for `APPLY_OPEN`, `APPLY_ACTIVE`, `CLOSING_SOON`, and duplicate suppression.
- [ ] Implement date parsing and event selection.
- [ ] Implement JavaMailSender delivery and successful-send logging.
- [ ] Add daily scheduler and manual authenticated trigger endpoint.
- [ ] Run targeted tests.

### Task 5: Security And Verification

**Files:**
- Modify: `src/main/java/com/happyhome/config/SecurityConfig.java`
- Test: `src/test/java/com/happyhome/config/SpaFrontendIntegrationTest.java`

- [ ] Require authentication for `/api/rentals/favorites/**`, `/api/rentals/recommendations`, and favorite toggle endpoints.
- [ ] Verify anonymous write access is rejected.
- [ ] Run `mvnw.cmd test` after unrelated compile errors are resolved.

## Self Review

- The plan covers favorite storage, recommendation APIs, mail sending, duplicate logging, scheduling, security, and tests.
- No placeholders are left.
- Package names and endpoint paths match the design.
