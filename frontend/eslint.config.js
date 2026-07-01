import js from '@eslint/js';
import pluginVue from 'eslint-plugin-vue';
import globals from 'globals';

export default [
  {
    ignores: ['node_modules/', 'dist/', 'build/', 'webapp/', 'public/'],
  },
  {
    languageOptions: {
      globals: globals.browser,
    },
  },
  js.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    rules: {
      'no-unused-vars': ['warn'],
      'vue/max-attributes-per-line': 'off',
      'vue/html-self-closing': [
        'error',
        {
          html: {
            void: 'always', // hr, br 같은 태그도 항상 />를 붙이도록 설정 (Prettier와 일치)
            normal: 'never',
            component: 'always',
          },
          svg: 'always',
          math: 'always',
        },
      ],
      // 한 줄짜리 요소의 줄바꿈 무시 - prettier 충돌
      'vue/singleline-html-element-content-newline': 'off',
      // 다중 라인 규칙 무시 - prettier 충돌
      'vue/multiline-html-element-content-newline': 'off',
    },
  },
];
