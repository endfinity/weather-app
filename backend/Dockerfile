# ──────────────────────────────────────────────
# Stage 1 — build (install deps + compile native modules)
# ──────────────────────────────────────────────
FROM node:22-bookworm-slim AS build

RUN apt-get update \
    && apt-get install -y --no-install-recommends dumb-init \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm ci --omit=dev

# ──────────────────────────────────────────────
# Stage 2 — production (minimal image)
# ──────────────────────────────────────────────
FROM node:22-bookworm-slim

ENV NODE_ENV=production

COPY --from=build /usr/bin/dumb-init /usr/bin/dumb-init

WORKDIR /usr/src/app

# Copy production node_modules from build stage
COPY --chown=node:node --from=build /usr/src/app/node_modules ./node_modules

# Copy application source
COPY --chown=node:node src/ ./src/
COPY --chown=node:node package.json ./

# Create data directory for SQLite (owned by non-root user)
RUN mkdir -p data && chown node:node data

USER node

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD node -e "fetch('http://localhost:3000/api/health').then(r=>{if(!r.ok)throw r;process.exit(0)}).catch(()=>process.exit(1))"

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["node", "src/index.js"]
