import { defineConfig } from 'vite';
import legacy from '@vitejs/plugin-legacy';
import vue from '@vitejs/plugin-vue';
import { viteSingleFile } from 'vite-plugin-singlefile';

export default defineConfig({
    base: './',
    plugins: [
        vue(),
        legacy({
            targets: ['defaults', 'not IE 11'],
        }),
        // 将构建结果压缩为单文件，方便 file:// 直接打开
        viteSingleFile(),
    ],
    build: {
        outDir: 'dist',
        assetsDir: 'assets',
    }
});
