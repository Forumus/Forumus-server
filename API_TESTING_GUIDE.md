# API Testing Guide - Post Summarize Endpoint

## Problem
You're getting a 404 error when calling `/api/posts/summarize` because the deployed server on AWS (3.105.149.245:8081) doesn't have the latest code with the summarize endpoint.

## Solution: Step-by-Step Testing Process

### Step 1: Verify the Endpoint Exists Locally ✅
The endpoint exists in your code:
- **File**: `src/main/java/com/hcmus/forumus_backend/controller/PostController.java`
- **Path**: `/api/posts/summarize`
- **Method**: POST
- **Request Body**: `{"postId": "POST_ID_HERE"}`

### Step 2: Test the API Locally

#### Option A: Using Docker (Recommended - Matches Production)

1. **Make sure you have the `.env` file** in the Forumus-server directory with your environment variables.

2. **Build and start the Docker container:**
   ```powershell
   cd "d:\hcmus-fifth-semester-practice\Mobile Application Development\Final Project\Source\Forumus-server"
   docker-compose up --build
   ```

3. **Wait for the container to start** - Look for this message:
   ```
   Started ForumusBackendApplication in X.XXX seconds
   ```
   The server will run on `http://localhost:8081` (mapped from container port 3000).

4. **Test with curl (PowerShell):**
   ```powershell
   curl -X POST http://localhost:8081/api/posts/summarize `
     -H "Content-Type: application/json" `
     -d '{"postId":"POST_20260125_133146_3880"}'
   ```

5. **Or use Postman:**
   - Method: POST
   - URL: `http://localhost:8081/api/posts/summarize`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "postId": "POST_20260125_133146_3880"
     }
     ```

6. **To stop the container:**
   ```powershell
   docker-compose down
   ```

7. **To view logs:**
   ```powershell
   docker-compose logs -f forumus-backend
   ```

#### Option B: Using Spring Boot Directly

1. **Start your local server:**
   ```powershell
   cd "d:\hcmus-fifth-semester-practice\Mobile Application Development\Final Project\Source\Forumus-server"
   .\mvnw.cmd spring-boot:run
   ```

2. **Wait for the server to start** - Look for this message:
   ```
   Started ForumusBackendApplication in X.XXX seconds
   ```
   The server will run on `http://localhost:8080` by default.

3. **Test with curl (PowerShell):**
   ```powershell
   curl -X POST http://localhost:8080/api/posts/summarize `
     -H "Content-Type: application/json" `
     -d '{"postId":"POST_20260125_133146_3880"}'
   ```

#### Option C: Using Maven Test

Run the existing tests to verify the endpoint works:
```powershell
cd "d:\hcmus-fifth-semester-practice\Mobile Application Development\Final Project\Source\Forumus-server"
.\mvnw.cmd test -Dtest=PostControllerSummarizeTest
```

#### Quick Docker Commands Reference

**Check if container is running:**
```powershell
docker ps
```

**Check container logs:**
```powershell
docker logs forumus-backend
```

**Restart container:**
```powershell
docker-compose restart
```

**Rebuild after code changes:**
```powershell
docker-compose up --build -d
```

**Stop and remove containers:**
```powershell
docker-compose down
```

### Step 3: Check Git Status

1. **Check if your changes are committed:**
   ```powershell
   cd Forumus-server
   git status
   ```

2. **Check your current branch:**
   ```powershell
   git branch
   ```

3. **See recent commits:**
   ```powershell
   git log --oneline -5
   ```

4. **Check if changes are in main:**
   ```powershell
   git log main --oneline -5
   ```

### Step 4: Merge and Deploy to Main (If Not Already)

1. **Commit your changes (if not committed):**
   ```powershell
   git add .
   git commit -m "Add post summarize endpoint"
   ```

2. **Switch to main branch:**
   ```powershell
   git checkout main
   ```

3. **Merge your feature branch:**
   ```powershell
   git merge your-feature-branch-name
   ```

4. **Push to remote:**
   ```powershell
   git push origin main
   ```

### Step 5: Verify Deployment to AWS

1. **Check GitHub Actions** (if you have CI/CD setup):
   - Go to your repository on GitHub
   - Click on "Actions" tab
   - Check if the deployment workflow is running/completed
   - The workflows are in `.github/workflows/`

2. **Check deployment status:**
   - Look for `deploy-aws.yml` or `docker-build.yml` workflows
   - Ensure they completed successfully

3. **Wait for deployment** (usually 5-10 minutes after push)

4. **Test the deployed endpoint:**
   ```powershell
   curl -X POST http://3.105.149.245:8081/api/posts/summarize `
     -H "Content-Type: application/json" `
     -d '{"postId":"POST_20260125_133146_3880"}'
   ```

### Step 6: Test from Android App

Once the server is deployed, your Android app should work. To verify:

1. **Check the base URL in your Android app:**
   - Look for `BASE_URL` or `API_URL` in your client code
   - It should point to: `http://3.105.149.245:8081`

2. **Run the app and try the summarize feature**

3. **Check Logcat for the response**

## Troubleshooting

### If Local Server Won't Start

**Check port 8080:**
```powershell
netstat -ano | findstr :8080
```

If it's in use, kill the process or change the port in `application.properties`:
```properties
server.port=8081
```

### If You Get Firebase Errors

Ensure `service_account.json` exists in `src/main/resources/`

### If Deployment Doesn't Happen Automatically

**Manual deployment option:**
1. Build the JAR:
   ```powershell
   .\mvnw.cmd clean package -DskipTests
   ```

2. The JAR will be in `target/` folder

3. Deploy manually to AWS (you'll need AWS credentials)

## Quick Verification Checklist

- [ ] Endpoint exists in `PostController.java`
- [ ] Local server starts without errors
- [ ] Local endpoint returns valid response (not 404)
- [ ] Changes are committed to git
- [ ] Changes are merged to main branch
- [ ] Changes are pushed to GitHub
- [ ] CI/CD pipeline completed successfully
- [ ] AWS server has been updated (wait 5-10 minutes)
- [ ] Remote endpoint returns valid response
- [ ] Android app successfully calls the endpoint

## Expected Response Format

**Success (200):**
```json
{
  "postId": "POST_20260125_133146_3880",
  "summary": "This is the AI-generated summary of the post...",
  "error": null
}
```

**Error (404 or 500):**
```json
{
  "postId": "POST_20260125_133146_3880",
  "summary": null,
  "error": "Error message here"
}
```

## Next Steps

1. **Start local server first** to confirm endpoint works
2. **Check git status** to see if changes are committed
3. **Merge to main** if on a feature branch
4. **Push to trigger deployment**
5. **Wait for deployment** to complete
6. **Test remote endpoint** again
