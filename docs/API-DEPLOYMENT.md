# Lamongan Bekas API — deployment setup

API runs on the VPS; Neon remains the database. Do not put DATABASE_URL in the Android APK.

## One-time setup (manual)
1. Verify the CubePath account and create a VPS only after approving the monthly price.
2. Point a hostname you control to the VPS public IP. The hostname must resolve before deployment so Caddy can obtain a valid HTTPS certificate.
3. Make sure the VPS user can run Docker Compose (root, or passwordless sudo configured).
4. In GitHub repository Settings → Secrets and variables → Actions, add these **Secrets**:
   - `VPS_HOST`: VPS public IP or hostname used for SSH.
   - `VPS_USER`: SSH login user.
   - `VPS_SSH_PRIVATE_KEY`: private SSH key matching a public key installed on that VPS.
   - `DATABASE_URL`: Neon connection string for a least-privilege role. The API only needs SELECT access to `public.categories`, `public.listings`, and `public.listing_photos`.
5. Add this **Variable**:
   - `API_DOMAIN`: hostname pointing to the VPS, without `https://` or a path.
6. Run the GitHub Actions workflow **Deploy Lamongan Bekas API to VPS** once.

## Automated flow
- API checks run on pushes/pull requests that change API files.
- The deploy workflow copies the API to the VPS, builds the container, starts it with Docker Compose, and Caddy provisions HTTPS.
- Set GitHub Actions Variable `API_BASE_URL` to `https://` followed by the same `API_DOMAIN`. The Android build workflow injects it into the APK automatically.
- After changing `API_BASE_URL`, run **Build Android APK** and install the resulting artifact.

## Endpoints
- `GET /health`
- `GET /categories`
- `GET /listings?limit=24&q=...`
- `GET /listings?kategori=elektronik`

No login, posting, messaging, or purchasing endpoints are implemented yet. Those are the next stage after public read endpoints work end-to-end.
