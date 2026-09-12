# 需要手动完成的步骤

以下步骤因沙箱文件系统限制无法自动执行，请手动操作：

## 1. 替换 pom.xml（Flyway 依赖）

`backend/pom.xml` 当前被锁定，无法直接修改。

**操作**：将 `backend/pom.xml.patched` 的内容复制到 `backend/pom.xml`，或手动在 `<!-- Lombok -->` 之前添加：

```xml
        <!-- Flyway 数据库迁移 -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>
```

## 2. 前端 ESLint + Prettier 配置

`frontend/` 根目录无法创建新文件。请手动创建以下文件：

### `frontend/.eslintrc.cjs`
```js
module.exports = {
  root: true,
  env: { browser: true, es2021: true, node: true },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended',
    'plugin:prettier/recommended'
  ],
  parserOptions: { ecmaVersion: 'latest', sourceType: 'module' },
  plugins: ['vue'],
  rules: {
    'vue/multi-word-component-names': 'off',
    'vue/no-v-html': 'off',
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off'
  }
}
```

### `frontend/.prettierrc`
```json
{
  "semi": true,
  "singleQuote": true,
  "tabWidth": 2,
  "trailingComma": "none",
  "printWidth": 100
}
```

### `frontend/.eslintignore`
```
node_modules
dist
*.min.js
```

### `frontend/.prettierignore`
```
node_modules
dist
package-lock.json
```

## 3. Git Hook（husky + lint-staged）

在项目根目录执行：

```bash
npm init -y
npm install --save-dev husky lint-staged
npx husky install
npx husky add .husky/pre-commit "npx lint-staged"
```

然后在 `package.json` 中添加：
```json
"lint-staged": {
  "src/**/*.{vue,js}": ["eslint --fix", "prettier --write"]
}
```

## 4. 删除锁定的临时脚本

以下 5 个文件被其他进程占用，请关闭占用程序后删除：

- `patch_redeploy.py`
- `pull_dataease.py`
- `pull_dataease_aliyun.py`
- `verify_decrypt_e2e.py`
- `verify_full.py`

## 5. 安装前端依赖

```bash
cd frontend
npm install
```

然后验证 lint 是否工作：
```bash
npm run lint
```