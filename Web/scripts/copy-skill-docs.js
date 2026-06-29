import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const skillDocsDir = path.resolve(__dirname, '../../skill-docs');
const distDir = path.resolve(__dirname, '../dist');
const targetDir = path.join(distDir, 'skill-docs');

function ensureSource() {
    if (!fs.existsSync(skillDocsDir)) {
        console.error('未找到 skill-docs 目录，无法复制文档');
        process.exit(1);
    }
}

function ensureDist() {
    if (!fs.existsSync(distDir)) {
        console.error('未找到 dist 目录，请先执行 vite build');
        process.exit(1);
    }
}

function cleanTarget() {
    if (fs.existsSync(targetDir)) {
        fs.rmSync(targetDir, { recursive: true, force: true });
    }
}

function copyDocs() {
    fs.cpSync(skillDocsDir, targetDir, { recursive: true, force: true });
}

function main() {
    ensureSource();
    ensureDist();
    cleanTarget();
    console.log('正在复制技能文档到 dist/skill-docs...');
    copyDocs();
    console.log('技能文档复制完成');
}

main();
