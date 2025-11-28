import { defineConfig } from 'vite';
import legacy from '@vitejs/plugin-legacy';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
    base: './',
    plugins: [
        vue(),
        legacy({
            targets: ['defaults', 'not IE 11'],
        }),
    ],
    build: {
        outDir: 'dist',
        assetsDir: 'assets',
    }
});
