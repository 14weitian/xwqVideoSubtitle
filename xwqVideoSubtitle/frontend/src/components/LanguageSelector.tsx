import React from 'react';
import { systemApi } from '../services/api';
import type { LanguageOption } from '../types';

interface LanguageSelectorProps {
  selectedLanguage: string;
  onLanguageChange: (language: string) => void;
  className?: string;
}

const LanguageSelector: React.FC<LanguageSelectorProps> = ({
  selectedLanguage,
  onLanguageChange,
  className = '',
}) => {
  const languages = systemApi.getLanguages();

  return (
    <select
      value={selectedLanguage}
      onChange={(e) => onLanguageChange(e.target.value)}
      className={`px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`}
    >
      {languages.map((language) => (
        <option key={language.code} value={language.code}>
          {language.name} ({language.nativeName})
        </option>
      ))}
    </select>
  );
};

export default LanguageSelector;